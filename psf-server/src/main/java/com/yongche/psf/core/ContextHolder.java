package com.yongche.psf.core;

import java.nio.charset.Charset;

/**
 * Created by stony on 16/11/3.
 */
public class ContextHolder {

    public static final String PSF_DEFAULT_CHARSET              = "UTF-8";
    public static final Charset PSF_CHARSET_UTF8 = Charset.forName(PSF_DEFAULT_CHARSET);

    public static final int PSF_MAX_SERVICE_TYPE_SIZE           = 16;
    public static final int SERVICE_CENTER_HOST_NUMBER_MAX      = 8;
    public static final int PSF_PROTO_MAGIC_NUMBER              = 0x23232323;

    //Server加入ServerCenter
    public static final int PSF_PROTO_FID_SERVER_JOIN           = 65;
    public static final int PSF_PROTO_FID_SERVER_JOIN_RES       = 66;

    public static final int PSF_PROTO_FID_HEARTBEAT_SERVER      = 67;
    public static final int PSF_PROTO_FID_HEARTBEAT_SERVER_RES  = 68;

    public static final int PSF_PROTO_FID_ALLOC_SERVER          = 69;
    public static final int PSF_PROTO_FID_CLIENT_JOIN           = 71;
    public static final int PSF_PROTO_FID_CLIENT_JOIN_RES       = 72;
    public static final int PSF_PROTO_FID_CLIENT_BIND           = 79;
    public static final int PSF_PROTO_FID_CLIENT_BIND_RES       = 80;
    public static final int PSF_PROTO_FID_RPC_REQ_NO_HEADER     = 75;
    public static final int PSF_PROTO_FID_RPC_REQ_WITH_HEADER   = 81;
    public static final int PSF_PROTO_FID_RPC_REQ_RES           = 76;

    public static final int PSF_STATUS_SUCCEEDED                = 0;

    public static final int DEFAULT_IO_THREAD                   = Runtime.getRuntime().availableProcessors() + 1;
    public static final int DEFAULT_SERVICE_THREAD              = 50;

    private static final int DEFALUT_MAX_PKG_SIZE = 64 * 1024;
    public int max_pkg_size;
    public byte[] send_recv_buf;

    public int connect_timeout = 10000;
    public int network_timeout = 60000;
    public int reconnect_times = 3;

    public ContextHolder(int max_pkg_size) {
        this.max_pkg_size = max_pkg_size;
        this.send_recv_buf = new byte[max_pkg_size];;
    }


    public ContextHolder() {
        this(DEFALUT_MAX_PKG_SIZE);
    }
}
