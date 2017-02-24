package com.yongche.psf.client;

import com.alibaba.fastjson.JSON;
import com.yongche.psf.core.ConnectionInfo;
import com.yongche.psf.core.PackageBuilder;
import com.yongche.psf.core.ProtocolHeader;
import com.yongche.psf.exception.ClientException;
import com.yongche.psf.server.ServiceCenterManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import static com.yongche.psf.core.ContextHolder.PSF_CHARSET_UTF8;

/**
 * @author shihui
 * Created by stony on 16/11/10.
 */
public class ClientManager{

    String service_type;
    byte[] service_type_byte;
    String version;
    String[] serviceCenter;
    Client[] clients;
    Client currentClient;
    ServerAddress[] serverAddresses;
    ServerAddress[] serviceCenterAddresses;
    PackageBuilder packageBuilder;
    int reties = 3;
    final AbstractClient.ClientModel clientModel;

    public ClientManager(String service_type, String version, ServiceCenterManager serviceCenterManager) {
        this(service_type,version,serviceCenterManager,AbstractClient.ClientModel.OIO);
    }
    public ClientManager(String service_type, String version, ServiceCenterManager serviceCenterManager,AbstractClient.ClientModel clientModel) {
        this.service_type = service_type;
        this.service_type_byte = service_type.getBytes(PSF_CHARSET_UTF8);
        this.version = version;
        this.serviceCenter = serviceCenterManager.getServiceCenter();
        int serviceCenterLen = serviceCenter.length;
        serviceCenterAddresses = new ServerAddress[serviceCenterLen];
        for (int i = 0; i < serviceCenterLen; i++) {
            String[] server = serviceCenter[i].split("\\:", 2);
            serviceCenterAddresses[i] = new ServerAddress(server[0].trim(), Integer.parseInt(server[1].trim()));
        }
        this.clientModel = clientModel;
        this.packageBuilder = new PackageBuilder(512);
        initClient();
        System.out.println("ClientManager Choice Model : " + clientModel);
    }
    public static ClientManager newOioClientManager(String service_type, String version, ServiceCenterManager serviceCenterManager){
        return new ClientManager(service_type,version,serviceCenterManager,AbstractClient.ClientModel.OIO);
    }
    public static ClientManager newNioClientManager(String service_type, String version, ServiceCenterManager serviceCenterManager){
        return new ClientManager(service_type,version,serviceCenterManager,AbstractClient.ClientModel.NIO);
    }
    private void initClient(){
        try {
            ServerAddress serverAddress = tryAllocateFromServiceCenter();
            addServerAddress(serverAddress);
            if(clientModel == AbstractClient.ClientModel.OIO){
                currentClient = new OioClient(serverAddress.host, serverAddress.port, service_type, "1.0.0").run();
            }else{
                currentClient = new NioClient(serverAddress.host, serverAddress.port, service_type, "1.0.0").run();
            }
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public <T> T call(String serviceUri, Hashtable headers, String message, Class<T> clazz) throws Exception {
        return call(serviceUri, headers, message, true, clazz);
    }
    public <T> T call(String serviceUri, Hashtable headers, Object message, Class<T> clazz) throws Exception {
        return call(serviceUri, headers, JSON.toJSONString(message), true, clazz);
    }

    /**
     *
     * @param serviceUri 请求路径
     * @param headers 头消息
     * @param message body消息
     * @param checkClient 检查客户端连接
     * @param clazz the class of T
     * @param <T> the type of the desired object
     * @return an object of type T from the string
     * @throws Exception
     */
    public <T> T call(String serviceUri, Hashtable headers, String message, boolean checkClient, Class<T> clazz) throws Exception {
        return JSON.parseObject(call(serviceUri, headers, message, checkClient), clazz);
    }
    public String call(String serviceUri, Hashtable headers, Object message) throws Exception {
        return call(serviceUri, headers, JSON.toJSONString(message), true);
    }
    public String call(String serviceUri, Hashtable headers, String message) throws Exception {
        return call(serviceUri, headers, message, true);
    }
    /**
     *
     * @param serviceUri 请求路径
     * @param headers 头消息
     * @param message body消息
     * @param checkClient 检查客户端连接
     * @return String 字符串
     * @throws Exception
     */
    public String call(String serviceUri, Hashtable headers, String message, boolean checkClient) throws Exception {
        if(checkClient) checkClient();
        return currentClient.call(serviceUri, headers, message);
    }
    private void checkClient(){
        if(currentClient != null){
            if(!currentClient.isConnected()){
                currentClient.close();
                initClient();
            }
        }else{
            initClient();
        }
    }
    public ServerAddress tryAllocateFromServiceCenter() throws Exception{
        ServerAddress serverAddress = null;
        for (int i = 0; i < reties; i++) {
            try {
                serverAddress = allocateFromServiceCenter();
            } catch (Exception e) {
                System.out.println("allocate server address error : " + e.getMessage());
            }
            if(serverAddress != null) break;
            else sleep(3000);
        }

        if(null == serverAddress){
            throw new ClientException("failed allocate from service center.");
        }
        return serverAddress;
    }
    public static void sleep(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            System.out.println("sleep error : " + e.getMessage());
        }
    }
    private ServerAddress allocateFromServiceCenter() throws IOException{
        ServerAddress serverAddress = null;
        boolean ret;
        byte[] recvBuff = null;
        ConnectionInfo serviceCenterConn = null;
        try {
            serviceCenterConn = connectServiceCenter();
            if (null == serviceCenterConn) {
                throw new RuntimeException("failed to connect to service center");
            }
            int len = packageBuilder.buildAllocatePackage(service_type);
            OutputStream out = serviceCenterConn.sock.getOutputStream();
            out.write(packageBuilder.context.send_recv_buf, 0, len);
            InputStream in = serviceCenterConn.sock.getInputStream();

            ProtocolHeader header = packageBuilder.recvHeader(in);
            if (header.body_len > 0) {
                recvBuff = packageBuilder.readFully(in, header.body_len);
            }
            if (header.status != 0) {
                String error;
                error = "allocate server from service center recv package status " + header.status + " != 0";
                if (recvBuff != null) {
                    error += ", error info: " + new String(recvBuff, PSF_CHARSET_UTF8);
                }
                throw new IOException(error);
            }
            if (header.body_len <= 3) {
                throw new IOException("allocate server recv package body_len " + header.body_len + " <= 3");
            }
            int port = packageBuilder.buff2short(recvBuff, 0);
            String host = new String(recvBuff, 3, recvBuff[2]);
            serverAddress = new ServerAddress(host, port);
        }finally {
            if(serviceCenterConn != null) serviceCenterConn.close();
        }
        return serverAddress;
    }
    public void addClient(Client client){
        if(clients == null) {
            clients = new Client[1];
            clients[0] = client;
        } else {
            int old_len = clients.length;
            Client[] newClients = new Client[old_len++];
            for (int i = 0; i < old_len; i++) {
                newClients[i] = clients[i];
            }
            newClients[old_len] = client;
            clients = newClients;
        }
    }
    public void addServerAddress(ServerAddress serverAddress){
        if(serverAddresses == null) {
            serverAddresses = new ServerAddress[1];
            serverAddresses[0] = serverAddress;
        } else{
            int old_len = serverAddresses.length;
            ServerAddress[] newServerAddress = new ServerAddress[1+old_len];
            for (int i = 0; i < old_len; i++) {
                newServerAddress[i] = serverAddresses[i];
            }
            newServerAddress[old_len] = serverAddress;
            serverAddresses = newServerAddress;
        }
    }
    private ConnectionInfo connectServiceCenter(){
        ConnectionInfo connectionInfo = null;
        int serviceCenterLen = serviceCenterAddresses.length;
        for (int i = 0; i < serviceCenterLen; i++) {
            ServerAddress serverAddress = serviceCenterAddresses[i];
            connectionInfo = new ConnectionInfo(serverAddress.host, serverAddress.port);
            try{
                connectionInfo.connect(packageBuilder.context.connect_timeout,packageBuilder.context.network_timeout);
                return connectionInfo;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return connectionInfo;
    }
    /**
     * 释放资源
     */
    public void destroy(){
//        if(clients != null && clients.length > 0) {
//            for (Client client : clients) {
//                if (client != null) {
//                    client.close();
//                }
//            }
//        }
        if (currentClient != null) {
            currentClient.close();
        }
    }
    public boolean bind() throws Exception{
        checkClient();
        return currentClient.bind();
    }

    class ServerAddress {
        public String host;
        public int port;

        public ServerAddress(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServerAddress that = (ServerAddress) o;

            if (port != that.port) return false;
            return host != null ? host.equals(that.host) : that.host == null;

        }

        @Override
        public int hashCode() {
            int result = host != null ? host.hashCode() : 0;
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            return "ServerAddress{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    '}';
        }
    }
}
