package com.yongche.psf.server;

import com.yongche.psf.core.LoggerHelper;
import com.yongche.psf.core.PackageBuilder;
import com.yongche.psf.service.ServiceMappingInfo;
import com.yongche.psf.service.ServiceRequest;
import com.yongche.psf.core.ProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yongche.psf.core.ContextHolder.*;

/**
 * Created by stony on 16/11/2.
 */
public class ServerHandler extends SimpleChannelInboundHandler<ProtocolMessage> {

    Map<String, ServiceMappingInfo> urlMappings;
    ThreadPoolExecutor serviceExecutor;

    public ServerHandler(Map<String, ServiceMappingInfo> urlMappings,ThreadPoolExecutor serviceExecutor) {
        super();
        this.urlMappings = urlMappings;
        this.serviceExecutor = serviceExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage msg) throws Exception {
//        System.out.println("Channel : "+ctx.channel().id().asShortText()
//                +" ----- "+"From : " + ctx.channel().localAddress());

        boolean isFunOk = false;
        boolean isEmptyBody = false;
        boolean isRpc = false;
        String serviceUri = "";
        ServiceMappingInfo serviceMappingInfo = null;
        switch (msg.header.func_id){
            // no header rpc request 75        无HTTP Header请求
            case PSF_PROTO_FID_RPC_REQ_NO_HEADER :
                isRpc = true;
                ServerMonitor.getInstance().getReqTotalCount().incrementAndGet();
                msg.header.func_id = PSF_PROTO_FID_RPC_REQ_RES;
                if(msg.header.body_len > 0){
                    ProtocolMessage.RpcNoHeaderBody noHeaderBody = (ProtocolMessage.RpcNoHeaderBody) msg.body;
                    serviceUri = noHeaderBody.service_uri;
                    serviceMappingInfo = urlMappings.get(PackageBuilder.convertUri(noHeaderBody.service_uri));
                    if(serviceMappingInfo != null) {
                        Map<String, String> parameters = PackageBuilder.parseUri(serviceUri);
                        serviceMappingInfo.setRequest(new ServiceRequest(null, parameters, noHeaderBody.message));
                    }
                }else{
                    isEmptyBody = true;
                    isFunOk = true;
                }
                break;
            // have header rpc request 81  有Http Header请求
            case PSF_PROTO_FID_RPC_REQ_WITH_HEADER :
                isRpc = true;
                ServerMonitor.getInstance().getReqTotalCount().incrementAndGet();
                msg.header.func_id = PSF_PROTO_FID_RPC_REQ_RES;
                if(msg.header.body_len > 0){
                    ProtocolMessage.RpcBody headerBody = (ProtocolMessage.RpcBody) msg.body;
                    serviceUri = headerBody.service_uri;
                    serviceMappingInfo = urlMappings.get(PackageBuilder.convertUri(headerBody.service_uri));
                    if(serviceMappingInfo != null) {
                        Map<String, String> headers = new HashMap<>();
                        Map<String, String> parameters = PackageBuilder.parseUri(serviceUri);
                        if(headerBody.header_len > 0){
                            headers = PackageBuilder.parseParameters(headerBody.header);
                        }
                        serviceMappingInfo.setRequest(new ServiceRequest(headers, parameters, headerBody.message));
                    }
                }else{
                    isEmptyBody = true;
                    isFunOk = true;
                }
                break;
            //client join 71
            case PSF_PROTO_FID_CLIENT_JOIN :
                msg.header.func_id = PSF_PROTO_FID_CLIENT_JOIN_RES;
                isEmptyBody = true;
                isFunOk = true;
                break;
            //client bind 79
            case PSF_PROTO_FID_CLIENT_BIND :
                msg.header.func_id = PSF_PROTO_FID_CLIENT_JOIN_RES;
                isEmptyBody = true;
                isFunOk = true;
                ServerMonitor.getInstance().bindChannel(ctx.channel());
                break;
            default: break;
        }
        boolean isServiceTask = false;
        if(null != serviceMappingInfo){
            serviceExecutor.execute(new ServiceRequestTask(ctx, msg, serviceMappingInfo, serviceUri));
            isServiceTask = true;
            isFunOk = true;
        }else{
            // 无效的请求
            if(isRpc){
                String data = serviceUri + " is invalid request.";
                byte[] _data = data.getBytes(PSF_CHARSET_UTF8);
                msg.header.body_len = _data.length;
                msg.header.status = 3;
                msg.header.magic_number = 0;
                msg.body = _data;
                isFunOk = true;
                ServerMonitor.getInstance().getReqServerErrorCount().incrementAndGet();
            }
        }
        if(isEmptyBody){
            msg.header.body_len = 0;
            msg.header.status = 0;
            msg.header.magic_number = 0;
            msg.body = null;
        }
        if(isFunOk) {
            if(!isServiceTask) {
                // 写入 RPC 响应对象
                ctx.writeAndFlush(msg);
            }
        } else {
            // discard RPC
            ServerMonitor.getInstance().getReqDiscardCount().incrementAndGet();
        }
    }

    private String handler(ServiceMappingInfo serviceMappingInfo) throws Throwable{
        return serviceMappingInfo.invoke();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        ServerMonitor.getInstance().getReqDisconnectCount().incrementAndGet();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ServerMonitor.getInstance().registerChannel(ctx);
        // 当 target_connections 大于 0 时，
        // 并且 server 连接数大于 target_connections时，需要踢掉多余的连接，保持多台server平衡
        // 而且要拒绝client 的 join
        if(ServerMonitor.getInstance().getOverConnectionCount() > 0){
            ServerMonitor.getInstance().discardChannel();
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ServerMonitor.getInstance().unregisterChannel(ctx);
        ServerMonitor.getInstance().unbindChannel(ctx.channel());
    }
    class ServiceRequestTask implements Runnable{
        ChannelHandlerContext ctx;
        ProtocolMessage msg;
        ServiceMappingInfo serviceMappingInfo;
        String serviceUri;

        /**
         * 服务请求线程处理
         * @param ctx
         * @param msg
         * @param serviceMappingInfo
         * @param serviceUri
         */
        public ServiceRequestTask(ChannelHandlerContext ctx, ProtocolMessage msg, ServiceMappingInfo serviceMappingInfo, String serviceUri) {
            this.ctx = ctx;
            this.msg = msg;
            this.serviceMappingInfo = serviceMappingInfo;
            this.serviceUri = serviceUri;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            try {
                // 正在处理的请求
                ServerMonitor.getInstance().getReqDoingCount().incrementAndGet();
                // method handler process
                String data = handler(serviceMappingInfo);
                if (data == null || data.length() == 0) {
                    msg.header.body_len = 0;
                    msg.header.status = 0;
                    msg.header.magic_number = 0;
                    msg.body = null;
                } else {
                    byte[] _date = data.getBytes(PSF_CHARSET_UTF8);
                    msg.header.body_len = _date.length;
                    msg.header.status = 0;
                    msg.header.magic_number = 0;
                    msg.body = _date;
                }
                ServerMonitor.getInstance().getReqDealCount().incrementAndGet();
            } catch (Throwable e){
                String data = serviceUri + " process error : " + e.getMessage();
                byte[] _data = data.getBytes(PSF_CHARSET_UTF8);
                msg.header.body_len = _data.length;
                msg.header.status = 5;
                msg.header.magic_number = 0;
                msg.body = _data;
                ServerMonitor.getInstance().getReqServerErrorCount().incrementAndGet();
            } finally {
                long time = System.currentTimeMillis() - startTime;
                ServerMonitor.getInstance().getReqTimeUsed().addAndGet(time);
                ServerMonitor.getInstance().getReqDoingCount().decrementAndGet();
            }
            try {
                ctx.writeAndFlush(msg);
            } catch (Throwable ex) {
                ctx.close();
                String em = "执行请求 " + serviceUri + " 写入异常[" + serviceMappingInfo + "]: ";
                LoggerHelper.error(em, ex);
            }
        }
    }
}
