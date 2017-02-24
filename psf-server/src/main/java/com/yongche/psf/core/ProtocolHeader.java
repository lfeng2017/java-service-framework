package com.yongche.psf.core;

/**
 * <h4>协议头说明</h4>
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=4 summary="私有协议头定义.">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *          <th align=left>字段名
 *          <th align=left>含义
 *          <th align=left>类型
 *          <th align=left>字节
 *     <tr valign=top>
 *          <td><code>magic_number</code>
 *          <td>魔法数字
 *          <td>int
 *          <td>4
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>func_id</code>
 *          <td>功能ID/命令码
 *          <td>byte
 *          <td>1
 *     <tr valign=top>
 *          <td><code>status</code>
 *          <td>状态码
 *          <td>byte
 *          <td>1
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>body_len</code>
 *          <td>body长度
 *          <td>int
 *          <td>4
 * </table>
 * </blockquote>
 * <h4>功能ID定义</h4>
 * <blockquote>
 * <table border=0 cellspacing=2 cellpadding=3 summary="功能定义.">
 *     <tr style="background-color: rgb(204, 204, 255);">
 *          <th align=left>功能ID
 *          <th align=left>含义
 *     <tr valign=top>
 *          <td><code>65</code>
 *          <td>server join SC
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>66</code>
 *          <td>server join SC
 *     <tr valign=top>
 *          <td><code>67</code>
 *          <td>server 心跳请求
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>68</code>
 *          <td>server 心跳响应
 *      <tr valign=top>
 *          <td><code>69</code>
 *          <td>client 请求SC分配server
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>70</code>
 *          <td>分配server响应
 *     <tr valign=top>
 *          <td><code>71</code>
 *          <td>client join server
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>72</code>
 *          <td>client join server响应
 *     <tr valign=top>
 *          <td><code>75</code>
 *          <td>client RPC请求(无Http Header)
 *     <tr style="vertical-align: top; background-color: rgb(238, 238, 255);">
 *          <td><code>81</code>
 *          <td>client RPC请求(有Http Header)
 *     <tr valign=top>
 *          <td><code>76</code>
 *          <td>server RPC响应
 * </table>
 * </blockquote>
 * Created by stony on 16/11/3.
 * @author stony
 */
public class ProtocolHeader {
    public int magic_number;
    public byte func_id;
    public byte status;
    public int body_len;

    @Override
    public String toString() {
        return "ProtocolHeader{" +
                "magic_number=" + magic_number +
                ", func_id=" + func_id +
                ", status=" + status +
                ", body_len=" + body_len +
                '}';
    }
}
