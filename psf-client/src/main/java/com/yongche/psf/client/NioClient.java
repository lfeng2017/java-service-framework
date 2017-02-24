package com.yongche.psf.client;


/**
 * @author shihui
 * Created by stony on 16/11/7.
 * @see com.yongche.psf.client.AbstractClient
 */
public class NioClient extends AbstractClient{

    public NioClient(String host, int port, String service_type, String version) {
        super(host, port, service_type, version, ClientModel.NIO);
    }

}
