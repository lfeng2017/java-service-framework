package com.yongche.psf.server;

import com.yongche.psf.core.ConnectionInfo;
import com.yongche.psf.core.LoggerHelper;
import com.yongche.psf.core.PackageBuilder;
import com.yongche.psf.core.ProtocolHeader;
import com.yongche.psf.exception.ServerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static com.yongche.psf.core.ContextHolder.PSF_DEFAULT_CHARSET;

/**
 * Created by stony on 16/11/3.
 */
public class ServerRegister {

    private String serviceCenter;
    private ConnectionInfo connectionInfo;
    private PackageBuilder packageBuilder;
    private OutputStream out;
    private InputStream in;
    private Thread heartThread;
    private int port;
    private String serviceType;
    private int[] versions = new int[3];
    private int joinCenterCount = 1;
    private int joinRetimes = 3;
    private volatile boolean isJoin = false;

    public ServerRegister(String serviceCenter, int port, String service_type, String version) throws Exception {
        this.serviceCenter = serviceCenter;
        this.port = port;
        this.serviceType = service_type;
        String[] server = serviceCenter.split("\\:", 2);
        if (server.length != 2) {
            throw new ServerException("\"serviceCenter\" is invalid, the correct format is host:port");
        }
        String[] _versionStr = version.split("\\.", 3);
        if(_versionStr.length != 3){
            throw new ServerException("\"version\" is invalid, the correct format is version_major.version_minor.version_patch");
        }
        for (int i = 0; i < _versionStr.length; i++) {
            versions[i] = Integer.parseInt(_versionStr[i]);
        }
        connectionInfo = new ConnectionInfo(server[0].trim(), Integer.parseInt(server[1].trim()));
        packageBuilder = new PackageBuilder(256);
    }

    public void join() throws Exception{
        ProtocolHeader header;
        try {
            connectionInfo.connect(packageBuilder.context.connect_timeout,packageBuilder.context.network_timeout);
            connectionInfo.setKeepAlive();
            int len = packageBuilder.buildServerJoinPackage(port, serviceType, versions);

            out = connectionInfo.sock.getOutputStream();
            out.write(packageBuilder.context.send_recv_buf, 0, len);
            out.flush();

            in = connectionInfo.sock.getInputStream();
            header = packageBuilder.recvHeader(in);
            if (header.status != 0) {
                throw new ServerException("recv header status != 0 from server center, join "+serviceCenter+" error.");
            }
            LoggerHelper.info("join {} success {} ,header = {}" , serviceCenter, joinCenterCount, header);
            isJoin = true;
            joinCenterCount++;
            //run heartbeat task
            heartThread = new Thread(new ServerHeartBeatTask());
            heartThread.start();
        }catch (IOException e){
            connectionInfo.close();
            closeHeartThread();
            isJoin = false;
            throw e;
        }
    }
    private void retryJoin() {
        for (int i = 0; i < joinRetimes; i++) {
            closeHeartThread();
            connectionInfo.close();
            try {
                join();
            } catch (Exception e) {
                isJoin = false;
                e.printStackTrace();
            }finally {
                if(isJoin){
                    break;
                }
                sleep(3000);
            }
        }

    }
    public static void sleep(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            LoggerHelper.warn("sleep error : {}", e.getMessage());
        }
    }
    private void closeHeartThread(){
        if (heartThread != null) {
            try {
                heartThread.interrupt();
            } catch (Throwable e){
            }
        }
    }

    class ServerHeartBeatTask implements Runnable{
        int heartBeatError = 0;
        boolean stop = false;
        String sockAddress = connectionInfo.sock.getLocalAddress().getHostName() + ":" + connectionInfo.sock.getLocalPort();
        @Override
        public void run() {
            while (!stop){
                ProtocolHeader header;
                try {
                    int len = packageBuilder.buildServerHeartBeatPackage();
                    out.write(packageBuilder.context.send_recv_buf, 0, len);
                    out.flush();
                    header = packageBuilder.recvHeader(in);
                    byte[] recvBuff = null;
                    if (header.body_len > 0) {
                        recvBuff = packageBuilder.readFully(in, header.body_len);
                    }
                    if (header.status != 0) {
                        String error = "heart beat to "+serviceCenter+" recv package status " + header.status + " != 0";
                        if (recvBuff != null) {
                            error += ", error info: " + new String(recvBuff, PSF_DEFAULT_CHARSET);
                        }
                        LoggerHelper.warn("[{}] heartbeat [{}] failed {}" ,sockAddress, serviceCenter, header);
                        throw new ServerException(error);
                    }
                    int target_connections = PackageBuilder.bytes2Int(recvBuff);
                    // 当 target_connections 大于 0 时，
                    // 并且 server 连接数大于 target_connections时，需要踢掉多余的连接，保持多台server平衡
                    // 而且要拒绝client 的 join
                    if(target_connections > 0) {
                        ServerMonitor.getInstance().getTargetConnections().set((long) target_connections);
                        LoggerHelper.info("[{}] heartbeat [{}] success ,目标连接数 ＝ {}", sockAddress, serviceCenter, PackageBuilder.bytes2Int(recvBuff));
                        discardConnections(target_connections);
                    }else{
                        ServerMonitor.getInstance().getTargetConnections().set(0L);
                    }
                    sleep(3000);
                }catch (IOException e){
                    connectionInfo.close();
                    LoggerHelper.warn("心跳异常 : {}", e.getMessage());
                    heartBeatError++;
                    if(heartBeatError == 3){
                        stop = true;
                    }
                }
            }
            if(stop){
                retryJoin();
            }
        }
        private void discardConnections(int target_connections){
            try{
                ServerMonitor.getInstance().discardChannel(target_connections);
            }catch (Exception e){
                LoggerHelper.warn("discard connections error : ", e.getMessage());
            }
        }
    }

}
