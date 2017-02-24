package com.yongche.psf.client;

import com.yongche.psf.core.ProtocolMessage;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.yongche.psf.core.ContextHolder.*;

/**
 * @author shihui
 * Created by stony on 16/11/7.
 */
public class ClientHandler extends SimpleChannelInboundHandler<ProtocolMessage> {


    ChannelFutureResultListener listener;

    public ClientHandler(ChannelFutureResultListener listener) {
        super(true);
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage msg) throws Exception {
//        System.out.println("Channel : "+ctx.channel().id().asShortText()
//                +" ----- "+"From : " + ctx.channel().localAddress());
        switch (msg.header.func_id){
            // RPC 响应 76
            case PSF_PROTO_FID_RPC_REQ_RES :
                if(PSF_STATUS_SUCCEEDED == msg.header.status){
                    String message = null;
                    if(msg.header.body_len > 0){
                        message = (String) msg.body;
                    }
                    listener.callback(new ChannelFutureResult(ctx.channel().newSucceededFuture(), message, PSF_STATUS_SUCCEEDED));
                }else{
                    listener.callback(new ChannelFutureResult(ctx.channel().newSucceededFuture(), (String) msg.body, 32));
                }
                break;
            //client join 响应 72
            case PSF_PROTO_FID_CLIENT_JOIN_RES :
                if(PSF_STATUS_SUCCEEDED == msg.header.status){
                    listener.callback(new ChannelFutureResult(ctx.channel().newSucceededFuture(), null, PSF_STATUS_SUCCEEDED));
                }else{
                    listener.callback(new ChannelFutureResult(ctx.channel().newSucceededFuture(), (String) msg.body, 31));
                }
                break;
            default: break;
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        listener.callback(new ChannelFutureResult(ctx.channel().newFailedFuture(cause),cause.getMessage(), 33));
        cause.printStackTrace();
        ctx.close();
    }

}
