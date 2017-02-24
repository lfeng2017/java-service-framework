package com.yongche.psf.client;

import java.util.Hashtable;

/**
 * @author shihui
 * Created by stony on 16/11/23.
 */
public interface Client {
    /**
     * 启动一个客户端
     * @return  com.yongche.psf.client.Client
     * @throws Exception
     */
    Client run() throws Exception;

    String call(String serviceUri, Hashtable headers, String message) throws Exception;

    boolean join() throws Exception;

    boolean bind() throws Exception;

    void close();

    boolean isConnected();
}
