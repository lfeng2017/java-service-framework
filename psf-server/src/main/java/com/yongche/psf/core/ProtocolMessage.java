package com.yongche.psf.core;

import com.yongche.psf.core.ProtocolHeader;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * <h4>无Http Hander请求说明</h4>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=4 summary="私有协议头定义.">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *          <th align=left>字段名
 *          <th align=left>含义
 *          <th align=left>类型
 *          <th align=left>字节
 *     <tr valign=top>
 *          <td><code>service_uri_len</code>
 *          <td>服务名称长度
 *          <td>short
 *          <td>2
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>service_uri</code>
 *          <td>服务请求路径
 *          <td>char
 *          <td>service_uri_len
 *     <tr valign=top>
 *          <td><code>message</code>
 *          <td>消息内容
 *          <td>char
 *          <td>body_len - 2 - service_uri_len
 * </table>
 * </blockquote>
 *
 * <h4>有Http Hander请求说明</h4>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=4 summary="私有协议头定义.">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *          <th align=left>字段名
 *          <th align=left>含义
 *          <th align=left>类型
 *          <th align=left>字节
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>header_len</code>
 *          <td>url参数长度
 *          <td>short
 *          <td>2
 *     <tr valign=top>
 *          <td><code>service_uri_len</code>
 *          <td>服务名称长度
 *          <td>short
 *          <td>2
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>service_uri</code>
 *          <td>服务请求路径
 *          <td>char
 *          <td>service_uri_len
 *     <tr valign=top>
 *          <td><code>message</code>
 *          <td>消息内容
 *          <td>char
 *          <td>body_len - 4 － header_len - service_uri_len
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>header</code>
 *          <td>url参数
 *          <td>char
 *          <td>header_len
 * </table>
 * </blockquote>
 *
 * Created by stony on 16/11/3.
 * @author stony
 *
 */
public class ProtocolMessage {

    public ProtocolHeader header;

    public Object body;

    public ProtocolMessage() {
    }

    public Object processBody(){
        return null;
    }
    public ProtocolHeader newProtocolHeader(){
        this.header = new ProtocolHeader();
        return this.header;
    }
    public ClientJoinBody newClientJoinBody(){
        return new ClientJoinBody();
    }
    public RpcNoHeaderBody newRpcNoHeaderBody(){
        return new RpcNoHeaderBody();
    }
    public RpcBody newRpcBody(){
        return new RpcBody();
    }
    @Override
    public String toString() {
        return "ProtocolMessage{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }

    public class ClientJoinBody{
        public byte service_type_len;
        public String service_type;

        @Override
        public String toString() {
            return "ClientJoinBody{" +
                    "service_type_len=" + service_type_len +
                    ", service_type='" + service_type + '\'' +
                    '}';
        }
    }
    public class RpcBody{
        public short header_len;
        public short service_uri_len;
        public String service_uri;
        /** body_len - 4 - header_len - service_uri_len **/
        public String message;
        /** header_len **/
        public String header;

        private Map<String,String> parameters;
        public Map<String,String> getParameters(){
            return parameters;
        }

        @Override
        public String toString() {
            return "RpcBody{" +
                    "header_len=" + header_len +
                    ", service_uri_len=" + service_uri_len +
                    ", service_uri='" + service_uri + '\'' +
                    ", message='" + message + '\'' +
                    ", header='" + header + '\'' +
                    ", parameters='" + parameters + '\'' +
                    '}';
        }
    }


    public class RpcNoHeaderBody{
        public short service_uri_len;
        public String service_uri;
        /** body_len - 2 - service_uri_len **/
        public String message;

        @Override
        public String toString() {
            return "RpcNoHeaderBody{" +
                    "service_uri_len=" + service_uri_len +
                    ", service_uri='" + service_uri + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
