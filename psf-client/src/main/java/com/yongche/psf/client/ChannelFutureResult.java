package com.yongche.psf.client;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.TimeUnit;

import static com.yongche.psf.core.ContextHolder.PSF_STATUS_SUCCEEDED;

/**
 * server返回结果
 * @author shihui
 * Created by stony on 16/11/8.
 */
public class ChannelFutureResult {

    ChannelFuture channelFuture;
    String message;
    int status;

    public ChannelFutureResult(ChannelFuture channelFuture, String message, int status) {
        this.channelFuture = channelFuture;
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
    public boolean isOk(){
        return status == PSF_STATUS_SUCCEEDED;
    }
    public boolean isSuccess(){
        return channelFuture.isSuccess();
    }

    public ChannelFuture sync() throws InterruptedException{
        return channelFuture.sync();
    }
}
