package com.yongche.psf.server;

import com.yongche.psf.exception.ServerException;
import com.yongche.psf.service.ServiceMappingInfo;
import com.yongche.psf.core.CodecAdapter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yongche.psf.core.ContextHolder.DEFAULT_IO_THREAD;
import static com.yongche.psf.core.ContextHolder.DEFAULT_SERVICE_THREAD;

/**
 * Created by stony on 16/11/2.
 */
public class Server {
    int port;
    String service_type;
    String version;
    String[] service_center;
    Map<String, ServiceMappingInfo> urlMappings;
    ThreadPoolExecutor serviceExecutor;
    int ioThreads = DEFAULT_IO_THREAD;

    public Server(int port, String service_type, String version, String[] service_center,
                  Map<String, ServiceMappingInfo> urlMappings, ThreadPoolExecutor serviceExecutor) {
        this.port = port;
        this.service_type = service_type;
        this.version = version;
        this.service_center = service_center;
        if(this.service_center == null){
            throw new ServerException("service center must be not null.");
        }
        this.urlMappings = urlMappings;
        this.serviceExecutor = serviceExecutor;
    }

    public void run()throws Exception{
        //该线程组是用于接受client端链接的，不同的线程组对应不同的协议，且nio是专门针对tcp协议的
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //该线程组是用于实际的业务处理操作
        final EventLoopGroup workerGroup = new NioEventLoopGroup(ioThreads);
        try {
            //创建一个辅助类，就是针对server进行一系列的配置
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
//                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            CodecAdapter codecAdapter = new CodecAdapter();
                            ch.pipeline()
                                    .addLast(codecAdapter.getFirstDecoder())
                                    .addLast(codecAdapter.getDecoder())
                                    .addLast(codecAdapter.getEncoder())
                                    .addLast(new ServerHandler(urlMappings, serviceExecutor));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
//                    .option(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // start the server
            ChannelFuture f = b.bind(port).sync();
            // server register server center
            for (int i = 0, len = service_center.length; i < len; i++) {
                new ServerRegister(service_center[i], port, service_type, version).join();
            }
            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    public void close(){

    }
    public void setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
    }
}
