package com.yongche.psf.client;

import com.yongche.psf.core.ProtocolMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.Hashtable;

/**
 * server返回结果回调监听
 * @author shihui
 * Created by stony on 16/11/22.
 * @see com.yongche.psf.client.ClientHandler#channelRead0(ChannelHandlerContext, ProtocolMessage)
 * @see com.yongche.psf.client.AbstractClient#call(String, Hashtable, String)
 */
public interface ChannelFutureResultListener {
    void callback(ChannelFutureResult channelFutureResult);
}
