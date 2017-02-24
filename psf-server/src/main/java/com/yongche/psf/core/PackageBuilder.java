package com.yongche.psf.core;

import com.yongche.psf.server.ServerMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static com.yongche.psf.core.ContextHolder.*;

/**
 * Created by stony on 16/11/3.
 */
public class PackageBuilder {

    public ContextHolder context;

    /**
     *
     * @param max_pkg_size 数据包缓冲区最大字节
     */
    public PackageBuilder(int max_pkg_size) {
        this.context = new ContextHolder(max_pkg_size);
    }

    public PackageBuilder() {
        this.context = new ContextHolder();
    }

    public int buildPsfProtoHeader(byte[] header, int func_id, int status, int body_len){
        byte[] hex_len;
        int offset;
        offset = 0;
        hex_len = int2buff(PSF_PROTO_MAGIC_NUMBER);
        System.arraycopy(hex_len, 0, header, offset, hex_len.length);
        offset += hex_len.length;
        header[offset++] = (byte)func_id;
        header[offset++] = (byte)status;
        hex_len = int2buff(body_len);
        System.arraycopy(hex_len, 0, header, offset, hex_len.length);
        offset += hex_len.length;
        return offset;
    }

    public int buildServerJoinPackage(int port, String service_type, int[] versions)throws UnsupportedEncodingException {
        byte[] data;

        /* port(2byte) */
        /* service_type_len(1byte) */
        /* service_type(N byte) */
        /* version_major(1byte) + version_minor(1byte) + version_patch(1byte) */
        int body_len = 2  + 1 + service_type.length() + 3;

        int offset = buildPsfProtoHeader(context.send_recv_buf, PSF_PROTO_FID_SERVER_JOIN, 0, body_len);

        //build body port
        data = short2buff((short) port);
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;
        // service_type_len
        context.send_recv_buf[offset++] = (byte) service_type.length();
        // service_type
        data = service_type.getBytes(PSF_DEFAULT_CHARSET);
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;
        // version 3byte
        context.send_recv_buf[offset++] = (byte) versions[0];
        context.send_recv_buf[offset++] = (byte) versions[1];
        context.send_recv_buf[offset++] = (byte) versions[2];
        return offset;
    }

    public int buildServerHeartBeatPackage() throws UnsupportedEncodingException {
        byte[] data;

        ServerMonitor serverMonitor = ServerMonitor.getInstance();
        /* alloc_connection(8byte) */
        /* weight(2byte) */
        int body_len = 98;
        int offset = buildPsfProtoHeader(context.send_recv_buf, PSF_PROTO_FID_HEARTBEAT_SERVER, 0, body_len);
        // alloc_connection 分配的连接数
        data = long2Bytes(serverMonitor.getAllocConnection().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // connection_count 当前连接数量
        data = long2Bytes(serverMonitor.getConnectionCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // max_connections 达到最大连接数
        data = long2Bytes(serverMonitor.getMaxConnections().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // req_total_count 总请求次数
        data = long2Bytes(serverMonitor.getReqTotalCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // req_deal_count 已经处理的请求数量
        data = long2Bytes(serverMonitor.getReqDealCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // req_waiting_count 等待处理对请求数
        data = long2Bytes(serverMonitor.getReqWaitingCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        //req_doing_count 正在处理的请求数量
        data = long2Bytes(serverMonitor.getReqDoingCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        //req_server_error_count   server错误请求
        data = long2Bytes(serverMonitor.getReqServerErrorCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        //req_disconnect_count 客户端异常断开的请求数量
        data = long2Bytes(serverMonitor.getReqDisconnectCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // req_discard_count 丢弃的请求数
        data = long2Bytes(serverMonitor.getReqDiscardCount().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // req_time_used 已处理请求消耗的时间(毫秒)
        data = long2Bytes(serverMonitor.getReqTimeUsed().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        // bind_connections 绑定连接数 #bind
        data = long2Bytes(serverMonitor.getBindConnections().get());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        //build weight
        data = short2buff(serverMonitor.getWeight());
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;
        return offset;
    }
    public int buildClientJoinPackage(String service_type) throws UnsupportedEncodingException {
        byte[] data;

        int body_len = 1 /* service_type_len(1byte) */ + service_type.length();

        int offset = buildPsfProtoHeader(context.send_recv_buf, PSF_PROTO_FID_CLIENT_JOIN, 0, body_len);

        context.send_recv_buf[offset++] = (byte) service_type.length();

        data = service_type.getBytes(PSF_DEFAULT_CHARSET);
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        return offset;
    }
    public static String buildHeaderString(Hashtable request_headers) {
        int header_count;
        if (request_headers != null && (header_count = request_headers.size()) > 0) {
            StringBuilder str = new StringBuilder();
            Enumeration e = request_headers.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                str.append(key + "=" + request_headers.get(key));
                if (--header_count > 0) {
                    str.append("&");
                }
            }
            return str.toString();
        }
        return null;
    }
    public int buildRpcRequestPackage(Hashtable request_headers,String request_data,String request_service_uri) throws UnsupportedEncodingException {
        int header_count;
        int header_len;
        int func_id;
        int body_len;
        byte[] buff;
        byte[] header;
        byte[] service_uri;
        byte[] data;
        String header_str = buildHeaderString(request_headers);
        if(header_str == null || header_str.length() == 0){
            header = null;
            header_len = 0;
        }else{
            header = header_str.getBytes(PSF_DEFAULT_CHARSET);
            header_len = header.length;
        }

        data = request_data.getBytes(PSF_DEFAULT_CHARSET);
        service_uri = request_service_uri.getBytes(PSF_DEFAULT_CHARSET);

        body_len = 2 + service_uri.length + data.length;
        if (header_len == 0) {
            func_id = PSF_PROTO_FID_RPC_REQ_NO_HEADER;
        } else {
            func_id = PSF_PROTO_FID_RPC_REQ_WITH_HEADER;
            body_len += 2 + header_len;
        }

        int offset = buildPsfProtoHeader(context.send_recv_buf,func_id, 0, body_len);

        if (header_len > 0) {
            buff = short2buff((short) header_len);
            System.arraycopy(buff, 0, context.send_recv_buf, offset, buff.length);
            offset += buff.length;
        }
        buff = short2buff((short)service_uri.length);
        System.arraycopy(buff, 0, context.send_recv_buf, offset, buff.length);
        offset += buff.length;

        System.arraycopy(service_uri, 0, context.send_recv_buf, offset, service_uri.length);
        offset += service_uri.length;

        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        if (header_len > 0) {
            System.arraycopy(header, 0, context.send_recv_buf, offset, header.length);
            offset += header.length;
        }
        return offset;
    }
    public int buildAllocatePackage(String service_type)throws UnsupportedEncodingException{
        byte[] data;
        int service_type_len = service_type.length();
        int body_len = 1/* service_type_len(1byte) */ + service_type_len;

        int offset = buildPsfProtoHeader(context.send_recv_buf,PSF_PROTO_FID_ALLOC_SERVER, 0, body_len);

        context.send_recv_buf[offset++] = (byte) service_type_len;
        data = service_type.getBytes(PSF_DEFAULT_CHARSET);
        System.arraycopy(data, 0, context.send_recv_buf, offset, data.length);
        offset += data.length;

        return offset;
    }
    public byte[] readFully(InputStream in, int expectLength) throws IOException {
        int remain;
        int bytes;
        byte[] data  = new byte[expectLength];
        remain = expectLength;
        while (remain > 0 && (bytes=in.read(data, expectLength - remain, remain)) > 0) {
            remain -= bytes;
        }

        if (remain != 0) {
            throw new IOException("readFully connection closed");
        }

        return data;
    }

    public ProtocolHeader recvHeader(InputStream in) throws IOException {
        int bytes;
        byte[] recv_buff = this.readFully(in, 10);

        ProtocolHeader header = new ProtocolHeader();
        header.magic_number = buff2int(recv_buff, 0);
        header.func_id = recv_buff[4];
        header.status = recv_buff[5];
        header.body_len = buff2int(recv_buff, 6);

        if (header.magic_number != PSF_PROTO_MAGIC_NUMBER) {
            throw new IOException("recv package magic_number " + header.magic_number + " != " + PSF_PROTO_MAGIC_NUMBER);
        }

        if (header.body_len < 0) {
            throw new IOException("recv package body_len " + header.body_len + " < 0");
        }

        return header;
    }
    public static Map<String,String> parseParameters(String parameters){
        if(parameters == null|| parameters.length() == 0) return null;
        Map<String,String> map = new HashMap<>();
        String[] vs = parameters.split("&");
        for(String v : vs){
            String[] ps = v.split("=", 2);
            if(ps.length == 1){
                map.put(ps[0], null);
            }
            if(ps.length == 2){
                map.put(ps[0], ps[1]);
            }
        }
        return map;
    }
    public static Map<String,String> parseUri(String uri){
        if(uri == null|| uri.length() == 0) return null;
        int index = uri.indexOf("?");
        if(index != -1){
            String parameters = uri.substring(index+1);
            return parseParameters(parameters);
        }
        return null;
    }
    public static String convertUri(String uri){
        int index = uri.indexOf("?");
        if(index != -1){
            return uri.substring(0,index);
        }
        return uri;
    }
    /**
     * buff convert to int
     * @param bs the buffer (big-endian)
     * @param offset the start position based 0
     * @return int number
     */
    public static int buff2int(byte[] bs, int offset)
    {
        return  (((int)(bs[offset] >= 0 ? bs[offset] : 256+bs[offset])) << 24) |
                (((int)(bs[offset+1] >= 0 ? bs[offset+1] : 256+bs[offset+1])) << 16) |
                (((int)(bs[offset+2] >= 0 ? bs[offset+2] : 256+bs[offset+2])) <<  8) |
                ((int)(bs[offset+3] >= 0 ? bs[offset+3] : 256+bs[offset+3]));
    }

    /**
     * int convert to buff (big-endian)
     * @param n number
     * @return 4 bytes buff
     */
    public static byte[] int2buff(int n){
        byte[] bs;

        bs = new byte[4];
        bs[0] = (byte)((n >> 24) & 0xFF);
        bs[1] = (byte)((n >> 16) & 0xFF);
        bs[2] = (byte)((n >> 8) & 0xFF);
        bs[3] = (byte)(n & 0xFF);

        return bs;
    }

    /**
     * buff convert to int
     * @param bs the buffer (big-endian)
     * @param offset the start position based 0
     * @return int number
     */
    public static int buff2short(byte[] bs, int offset){
        return (((int)(bs[offset] >= 0 ? bs[offset] : 256+bs[offset])) << 8) |
                ((int)(bs[offset+1] >= 0 ? bs[offset+1] : 256+bs[offset+1]));
    }

    public static byte[] short2buff(short n){
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) ((n >> 8) & 0xff);
        return b;
    }

    public static byte[] int2Bytes(int num) {
        byte[] byteNum = new byte[4];
        for (int ix = 0; ix < 4; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static int bytes2Int(byte[] byteNum) {
        int num = 0;
        for (int ix = 0; ix < 4; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    public static byte int2OneByte(int num) {
        return (byte) (num & 0x000000ff);
    }

    public static int oneByte2Int(byte byteNum) {
        //针对正数的int
        return byteNum > 0 ? byteNum : (128 + (128 + byteNum));
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    // 首字母大写
    public static String capital(String str){
        char[] array = str.toCharArray();
        if(String.valueOf(array[0]).matches("a-z")){
            array[0] -= 32;
            return String.valueOf(array);
        }
        return str;
    }
    // 首字母小写
    public static String minuscules(String str){
        char[] array = str.toCharArray();
        if(String.valueOf(array[0]).matches("A-Z")){
            array[0] += 32;
            return String.valueOf(array);
        }
        return str;
    }
}
