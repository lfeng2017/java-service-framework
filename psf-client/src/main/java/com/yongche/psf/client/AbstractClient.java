package com.yongche.psf.client;

import com.yongche.psf.core.CodecAdapter;
import com.yongche.psf.core.PackageBuilder;
import com.yongche.psf.core.ProtocolHeader;
import com.yongche.psf.core.ProtocolMessage;
import com.yongche.psf.exception.ClientException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Hashtable;

import static com.yongche.psf.core.ContextHolder.*;

/**
 * @author shihui
 * Created by stony on 16/11/23.
 */
public abstract class AbstractClient implements Client, ChannelFutureResultListener{

    String host;
    int port;
    String service_type;
    byte[] service_type_byte;
    String version;
    final EventLoopGroup group;
    Channel originChannel;
    ChannelFuture lastChannelFuture;
    final ClientHandler clientHandler;
    int joinReties = 3;
    final ClientModel clientModel;
    ChannelFutureResult callbackChannelFutureResult;
    final Object monitor = new Object();

    public AbstractClient(String host, int port, String service_type, String version) {
        this(host,port,service_type,version,ClientModel.OIO);
    }
    public AbstractClient(String host, int port, String service_type, String version, ClientModel clientModel) {
        this.host = host;
        this.port = port;
        this.service_type = service_type;
        this.service_type_byte = service_type.getBytes(PSF_CHARSET_UTF8);
        this.version = version;
        this.clientHandler = new ClientHandler(this);
        this.clientModel = clientModel;
        if(clientModel == ClientModel.OIO) {
            this.group = new OioEventLoopGroup(DEFAULT_IO_THREAD);
        }else{
            this.group = new NioEventLoopGroup(DEFAULT_IO_THREAD);
        }
    }
    @Override
    public Client run() throws Exception{
        // Configure the client.
        Bootstrap b = new Bootstrap().group(group);
        if(clientModel == ClientModel.OIO) {
            b.channel(OioSocketChannel.class);
        }else{
            b.channel(NioSocketChannel.class);
        }
        b.remoteAddress(host, port)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        CodecAdapter codecAdapter = new CodecAdapter();
                        ch.pipeline()
                                .addLast(codecAdapter.getFirstDecoder())
                                .addLast(codecAdapter.getDecoder())
                                .addLast(codecAdapter.getEncoder())
                                .addLast(clientHandler);
                    }
                });

        // Start the client.
        // ChannelFuture f = b.connect(host, port).sync();
        // originChannel = f.channel();
        originChannel = b.connect().syncUninterruptibly().channel();
        boolean isJoin = false;
        do{
            isJoin = join();
            if(isJoin) break;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println("join server["+getRemoteAddress()+"] retry---> " + e.getMessage());
            }
            joinReties--;
        } while (joinReties >= 0);
        if(!isJoin){
            close();
            throw new ClientException("Failed to join server " + getRemoteAddress());
        }
        return this;
    }

    private void send(ProtocolMessage msg) throws Throwable{
        // Wait until all messages are flushed before closing the channel.
        originChannel.writeAndFlush(msg).sync();
    }
    @Override
    public String call(String serviceUri, Hashtable headers, String message) throws Exception {
        if(message == null) message = "";
        int header_len = 0;
        int body_len;
        String _header_str = PackageBuilder.buildHeaderString(headers);
        byte[] _header_str_byte;
        byte[] message_byte = message.getBytes(PSF_CHARSET_UTF8);
        byte[] service_uri_byte = serviceUri.getBytes(PSF_CHARSET_UTF8);
        if(_header_str == null || _header_str.length() == 0){
            header_len = 0;
        }else{
            _header_str_byte = _header_str.getBytes(PSF_CHARSET_UTF8);
            header_len = _header_str_byte.length;
        }

        ProtocolMessage msg = new ProtocolMessage();
        ProtocolHeader header = new ProtocolHeader();
        header.magic_number = PSF_PROTO_MAGIC_NUMBER;
        header.status = 0;

        body_len = 2 + service_uri_byte.length + message_byte.length;
        // no header body
        if(header_len == 0){
            header.func_id = PSF_PROTO_FID_RPC_REQ_NO_HEADER;

            ProtocolMessage.RpcNoHeaderBody noHeaderBody = msg.newRpcNoHeaderBody();
            noHeaderBody.service_uri_len = (short) serviceUri.length();
            noHeaderBody.service_uri = serviceUri;
            noHeaderBody.message = message;
            msg.body = noHeaderBody;

        }else{
            header.func_id = PSF_PROTO_FID_RPC_REQ_WITH_HEADER;
            ProtocolMessage.RpcBody rpcBody = msg.newRpcBody();
            rpcBody.header_len = (short) header_len;
            rpcBody.service_uri_len = (short) service_uri_byte.length;
            rpcBody.service_uri = serviceUri;
            rpcBody.message = message;
            rpcBody.header = _header_str;
            msg.body = rpcBody;

            body_len += 2 + header_len;
        }
        header.body_len = body_len;
        msg.header = header;
        // Waits for the complete  response
        synchronized (monitor) {
            ChannelFuture future = originChannel.writeAndFlush(msg);
            if(ClientModel.OIO == clientModel){
                future.sync();
            }
            lastChannelFuture = future;
            monitor.wait();
        }
        ChannelFutureResult futureResult = callbackChannelFutureResult;
        futureResult.sync();
        if (!futureResult.isOk()) {
            throw new ClientException("Failed to call message " + message + " to " + getRemoteAddress());
        }
        return futureResult.getMessage();
    }
    @Override
    public boolean join() throws Exception {
        ChannelFutureResult futureResult = doJoinOrBind((byte) PSF_PROTO_FID_CLIENT_JOIN);
        System.out.println("Finished Join ["+getRemoteAddress()+"] : " + futureResult.isOk());
        return futureResult.isOk();
    }
    @Override
    public boolean bind() throws Exception {
        ChannelFutureResult futureResult = doJoinOrBind( (byte) PSF_PROTO_FID_CLIENT_BIND);
        System.out.println("Finished bind ["+getRemoteAddress()+"] : " + futureResult.isOk());
        return futureResult.isOk();
    }
    private ChannelFutureResult doJoinOrBind(byte funcId) throws Exception {
        ProtocolMessage msg = new ProtocolMessage();
        ProtocolHeader header = new ProtocolHeader();
        header.magic_number = PSF_PROTO_MAGIC_NUMBER;
        header.func_id = funcId;
        header.status = 0;
        header.body_len = 1 + service_type_byte.length;

        ProtocolMessage.ClientJoinBody joinBody = msg.newClientJoinBody();
        joinBody.service_type_len = (byte) service_type_byte.length;
        joinBody.service_type = service_type;

        msg.header = header;
        msg.body = joinBody;
        // Waits for the complete  response
        synchronized (monitor) {
            ChannelFuture future = originChannel.writeAndFlush(msg).sync();
            lastChannelFuture = future;
            monitor.wait();
        }
        ChannelFutureResult futureResult = callbackChannelFutureResult;
        futureResult.sync();
        return futureResult;
    }
    @Override
    public void close(){
        try {
            if(originChannel != null) {
                // Wait until the connection is closed.
                originChannel.close().syncUninterruptibly();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
    public String getRemoteAddress(){
        return host + ":" + port;
    }
    public boolean isActive(){
        if(this.originChannel == null) return false;
        return this.originChannel.isActive();
    }

    @Override
    public boolean isConnected(){
        if(this.originChannel == null) return false;
        return this.originChannel.isOpen();
    }

    @Override
    public void callback(ChannelFutureResult channelFutureResult) {
        synchronized (monitor) {
            callbackChannelFutureResult = channelFutureResult;
            monitor.notifyAll();
        }
    }
    enum ClientModel{
        OIO(1,"OIO"),
        NIO(2,"NIO");
        int code;
        String info;
        ClientModel(int code, String info) {
            this.code = code;
            this.info = info;
        }
    }
}
