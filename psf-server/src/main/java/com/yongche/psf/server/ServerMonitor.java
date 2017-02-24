package com.yongche.psf.server;

import com.yongche.psf.core.LoggerHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by stony on 16/11/3.
 */
public class ServerMonitor {

    private ServerMonitor(){}

    public static ServerMonitor getInstance(){
        return ServerMonitorHolder.INSTANCE;
    }
    /** 分配的连接数 **/
    private final AtomicLong allocConnection = new AtomicLong(10);
    /** 当前连接数 **/
    private final AtomicLong connectionCount = new AtomicLong(0);
    /** 目标 连接数 **/
    private final AtomicLong targetConnections = new AtomicLong(0);
    /** 达到最大的连接数 **/
    private final AtomicLong maxConnections = new AtomicLong(0);
    private final AtomicLong reqTotalCount = new AtomicLong(0);
    private final AtomicLong reqDealCount = new AtomicLong(0);
    /** 等待处理的请求 **/
    private final AtomicLong reqWaitingCount = new AtomicLong(0);
    private final AtomicLong reqDoingCount = new AtomicLong(0);
    private final AtomicLong reqServerErrorCount = new AtomicLong(0);
    /** 客户度异常断开请求 **/
    private final AtomicLong reqDisconnectCount = new AtomicLong(0);
    /** 丢弃的请求数 **/
    private final AtomicLong reqDiscardCount = new AtomicLong(0);
    private final AtomicLong reqTimeUsed = new AtomicLong(0);
    private final AtomicLong bindConnections = new AtomicLong(0);
    private short weight = 1;

    private static final ConcurrentMap<Channel, ChannelHandlerContext> channelMap = new ConcurrentHashMap<Channel, ChannelHandlerContext>();
    private static final ConcurrentMap<Channel, Boolean> bindChannelMap = new ConcurrentHashMap<Channel, Boolean>();

    public AtomicLong getAllocConnection() {
        return allocConnection;
    }

    public AtomicLong getConnectionCount() {
        return connectionCount;
    }

    public AtomicLong getMaxConnections() {
        return maxConnections;
    }

    public AtomicLong getReqTotalCount() {
        return reqTotalCount;
    }

    public AtomicLong getReqDealCount() {
        return reqDealCount;
    }

    public AtomicLong getReqWaitingCount() {
        return reqWaitingCount;
    }

    public AtomicLong getReqDoingCount() {
        return reqDoingCount;
    }

    public AtomicLong getReqServerErrorCount() {
        return reqServerErrorCount;
    }

    public AtomicLong getReqDisconnectCount() {
        return reqDisconnectCount;
    }

    public AtomicLong getReqDiscardCount() {
        return reqDiscardCount;
    }

    public AtomicLong getReqTimeUsed() {
        return reqTimeUsed;
    }

    public AtomicLong getBindConnections() {
        return bindConnections;
    }

    public AtomicLong getTargetConnections() {
        return targetConnections;
    }

    public short getWeight() {
        return weight;
    }

    public void setWeight(short weight) {
        this.weight = weight;
    }

    private static abstract class ServerMonitorHolder {
        protected static final ServerMonitor INSTANCE = new ServerMonitor();
    }


    public void registerChannel(ChannelHandlerContext ctx){
        channelMap.put(ctx.channel(), ctx);
        LoggerHelper.info("|->>>注册通道 : " + ctx.channel() + " ,当前连接 : " + getConnectionCount().incrementAndGet());
        getMaxConnections().incrementAndGet();
    }
    public void unregisterChannel(ChannelHandlerContext ctx){
        channelMap.remove(ctx.channel());
        LoggerHelper.info("|->>>注销通道 : " + ctx.channel() + " ,当前连接 : " + getConnectionCount().decrementAndGet());
    }
    public void discardChannel(){
        long targetConnections = ServerMonitor.getInstance().getTargetConnections().get();
        discardChannel((int) targetConnections);
    }
    public void discardChannel(int targetConnections){
        int size = channelMap.size();
        LoggerHelper.info("|->>>当前通道数量 : " + size);
        if(size > targetConnections){
            int count = size - targetConnections;
            for(Map.Entry<Channel, ChannelHandlerContext> entry : channelMap.entrySet()){
                if(closeRegisterChannel(entry.getValue())) {
                    count--;
                }
                if (count <= 0) {
                    break;
                }
            }
        }
    }
    private boolean isOpen(ChannelHandlerContext ctx){
        if(ctx == null) return false;
        if(ctx.channel() == null) return false;
        return ctx.channel().isOpen();
    }
    private synchronized boolean closeRegisterChannel(ChannelHandlerContext ctx){
        try{
            Boolean isBind = bindChannelMap.get(ctx.channel());
            if(isBind == null){
                ctx.close(); //.sync();
                return true;
            }
        }catch (Throwable e){
            LoggerHelper.warn("关闭通道错误  : " + ctx);
            e.printStackTrace();
            return false;
        }
        return false;
    }
    public int getOverConnectionCount(){
        long targetConnections = getTargetConnections().get();
        if(targetConnections > 0 && getConnectionCount().get() > targetConnections){
            return (int) (getConnectionCount().get() - targetConnections);
        }
        return 0;
    }

    public void bindChannel(Channel channel){
        bindChannelMap.put(channel, Boolean.TRUE);
        LoggerHelper.info("|->>>绑定通道 : " + channel + " ,当前绑定 : " + getBindConnections().incrementAndGet());
    }
    public void unbindChannel(Channel channel){
        Boolean isBind = bindChannelMap.remove(channel);
        if(isBind != null) {
            LoggerHelper.info("|->>>解绑通道 : " + channel + " ,当前绑定 : " + getBindConnections().decrementAndGet());
        }
    }
}
