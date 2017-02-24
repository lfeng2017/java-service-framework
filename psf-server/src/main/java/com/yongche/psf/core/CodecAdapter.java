package com.yongche.psf.core;

import com.yongche.psf.server.ServerMonitor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

import static com.yongche.psf.core.ContextHolder.*;

/**
 * Created by stony on 16/11/2.
 */
public class CodecAdapter {

    private final ChannelOutboundHandler encoder = new CodecAdapter.InternalEncoder();

    private final ChannelInboundHandler decoder = new CodecAdapter.InternalDecoder();

    private final ChannelInboundHandler firstDecoder = new LengthFieldBasedFrameDecoder((1024*1024*125), 6, 4, 0, 0);

    public io.netty.channel.ChannelHandler getEncoder() {
        return encoder;
    }

    public io.netty.channel.ChannelHandler getDecoder() {
        return decoder;
    }
    public io.netty.channel.ChannelHandler getFirstDecoder() {
        return firstDecoder;
    }

    /**
     * 编码
     */
    private class InternalEncoder extends MessageToByteEncoder<ProtocolMessage> {
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, ProtocolMessage msg, ByteBuf out) throws Exception {

            switch (msg.header.func_id){
                // no header rpc request 75        无HTTP Header请求
                case PSF_PROTO_FID_RPC_REQ_NO_HEADER :
                    out.writeInt(PSF_PROTO_MAGIC_NUMBER);
                    out.writeByte(msg.header.func_id);
                    out.writeByte(msg.header.status);
                    out.writeInt(msg.header.body_len);

                    ProtocolMessage.RpcNoHeaderBody noHeaderBody = (ProtocolMessage.RpcNoHeaderBody) msg.body;
                    out.writeShort(noHeaderBody.service_uri_len);
                    out.writeCharSequence(noHeaderBody.service_uri, PSF_CHARSET_UTF8);
                    out.writeCharSequence(noHeaderBody.message, PSF_CHARSET_UTF8);
                    break;
                // have header rpc request 81      有Http Header请求
                case PSF_PROTO_FID_RPC_REQ_WITH_HEADER :
                    out.writeInt(PSF_PROTO_MAGIC_NUMBER);
                    out.writeByte(msg.header.func_id);
                    out.writeByte(msg.header.status);
                    out.writeInt(msg.header.body_len);

                    ProtocolMessage.RpcBody headerBody = (ProtocolMessage.RpcBody) msg.body;
                    out.writeShort(headerBody.header_len);
                    out.writeShort(headerBody.service_uri_len);
                    out.writeCharSequence(headerBody.service_uri, PSF_CHARSET_UTF8);
                    out.writeCharSequence(headerBody.message, PSF_CHARSET_UTF8);
                    out.writeCharSequence(headerBody.header, PSF_CHARSET_UTF8);
                    break;
                // RPC 响应 76
                case PSF_PROTO_FID_RPC_REQ_RES :
                    out.writeInt(PSF_PROTO_MAGIC_NUMBER);
                    out.writeByte(msg.header.func_id);
                    out.writeByte(msg.header.status);
                    out.writeInt(msg.header.body_len);
                    if(msg.header.body_len > 0) {
                        out.writeBytes((byte[]) msg.body);
                    }
                    break;
                //client join request 71
                //client bind request 79
                case PSF_PROTO_FID_CLIENT_JOIN :
                case PSF_PROTO_FID_CLIENT_BIND :
                    out.writeInt(PSF_PROTO_MAGIC_NUMBER);
                    out.writeByte(msg.header.func_id);
                    out.writeByte(msg.header.status);
                    out.writeInt(msg.header.body_len);
                    ProtocolMessage.ClientJoinBody joinBody = (ProtocolMessage.ClientJoinBody) msg.body;
                    int body_len = joinBody.service_type_len + joinBody.service_type.getBytes(PSF_CHARSET_UTF8).length;
                    //write body
                    out.writeByte(joinBody.service_type_len);
                    out.writeCharSequence(joinBody.service_type, PSF_CHARSET_UTF8);
                    break;
                //client join 响应 72
                case PSF_PROTO_FID_CLIENT_JOIN_RES :
                    out.writeInt(PSF_PROTO_MAGIC_NUMBER);
                    out.writeByte(msg.header.func_id);
                    if(ServerMonitor.getInstance().getOverConnectionCount() > 0){
                        out.writeByte(7);
                        out.writeCharSequence("EOVERFLOW: 服务器连接过多", PSF_CHARSET_UTF8);
                    }else {
                        out.writeByte(msg.header.status);
                        out.writeInt(0);
                    }
                    break;
                //client bind 响应 80
                case PSF_PROTO_FID_CLIENT_BIND_RES :
                    out.writeInt(PSF_PROTO_MAGIC_NUMBER);
                    out.writeByte(msg.header.func_id);
                    out.writeByte(msg.header.status);
                    out.writeInt(0);
                    break;
                default: break;
            }
        }
    }

    /**
     * 解码
     */
    private class InternalDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            // Wait until the header length is available.
//            if (in.readableBytes() < 10) {
//                return;
//            }
//            in.markReaderIndex();

            ProtocolMessage protocolMessage = new ProtocolMessage();
            ProtocolHeader header = new ProtocolHeader();
            header.magic_number = in.readInt();
            header.func_id = in.readByte();
            header.status = in.readByte();
            header.body_len = in.readInt();

            protocolMessage.header = header;
            Object body = null;
            int msg_len;
            switch (header.func_id){
                // no header rpc request 75        无HTTP Header请求
                case PSF_PROTO_FID_RPC_REQ_NO_HEADER :
                    ProtocolMessage.RpcNoHeaderBody noHeaderBody = protocolMessage.newRpcNoHeaderBody();
                    noHeaderBody.service_uri_len = in.readShort();
                    noHeaderBody.service_uri = (String) in.readCharSequence(noHeaderBody.service_uri_len, PSF_CHARSET_UTF8);
                    msg_len = header.body_len - 2 - noHeaderBody.service_uri_len;
                    noHeaderBody.message = (String) in.readCharSequence(msg_len, PSF_CHARSET_UTF8);
                    body = noHeaderBody;
                    break;
                // have header rpc request 81      有Http Header请求
                case PSF_PROTO_FID_RPC_REQ_WITH_HEADER :
                    ProtocolMessage.RpcBody rpcBody = protocolMessage.newRpcBody();
                    rpcBody.header_len = in.readShort();
                    rpcBody.service_uri_len = in.readShort();
                    rpcBody.service_uri = (String) in.readCharSequence(rpcBody.service_uri_len, PSF_CHARSET_UTF8);
                    msg_len = header.body_len - 4 - rpcBody.header_len - rpcBody.service_uri_len;
                    rpcBody.message = (String) in.readCharSequence(msg_len, PSF_CHARSET_UTF8);
                    rpcBody.header = (String) in.readCharSequence(rpcBody.header_len, PSF_CHARSET_UTF8);
                    body = rpcBody;
                    break;
                // RPC response 76
                case PSF_PROTO_FID_RPC_REQ_RES :

                    if(header.body_len > 0){
                        body = (String) in.readCharSequence(header.body_len, PSF_CHARSET_UTF8);
                    } else {
                        body = null;
                    }
                    break;
                //client join request 71
                //client bind request 79
                case PSF_PROTO_FID_CLIENT_JOIN :
                case PSF_PROTO_FID_CLIENT_BIND :
                    ProtocolMessage.ClientJoinBody joinBody = protocolMessage.newClientJoinBody();
                    joinBody.service_type_len = in.readByte();
                    joinBody.service_type = (String) in.readCharSequence(joinBody.service_type_len, PSF_CHARSET_UTF8);
                    body = joinBody;
                    break;
                // client join response 72
                // client bind response 80
                case PSF_PROTO_FID_CLIENT_JOIN_RES :
                case PSF_PROTO_FID_CLIENT_BIND_RES :
                    if(header.body_len > 0){
                        header.status = 3;
                        body = (String) in.readCharSequence(header.body_len, PSF_CHARSET_UTF8);
                    }
                    break;
                default: break;
            }
            protocolMessage.body = body;
            out.add(protocolMessage);
        }
    }
}
