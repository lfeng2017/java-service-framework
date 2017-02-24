package com.yongche.psf.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by stony on 16/11/3.
 */
public class ConnectionInfo {

    public Socket sock;
    String ip_addr;
    int port;

    boolean reallocate;
    boolean forbidden_alloc_server;

    public ConnectionInfo(String ip_addr, int port) {
        this.ip_addr = ip_addr;
        this.port = port;
        this.reallocate = true;
        this.forbidden_alloc_server = false;
    }

    public void connect(int connectTimeout, int networkTimeout) throws IOException {
        if (this.sock == null) {
            this.sock = new Socket();
            try {
                this.sock.setSoTimeout(connectTimeout);
                this.sock.connect(new InetSocketAddress(this.ip_addr, this.port), networkTimeout);
            } catch (IOException ex) {
                this.close();
                throw ex;
            }
        }
    }
    public void setKeepAlive(){
        try {
            if(this.sock != null){
                this.sock.setKeepAlive(true);
            }
        } catch (SocketException e) {
            LoggerHelper.warn("set keep alive error : {}", e.getMessage());
        }
    }
    public void reconnect()throws IOException{
        try {
            this.sock.connect(new InetSocketAddress(this.ip_addr, this.port));
            this.sock.setKeepAlive(true);
        } catch (IOException ex) {
            this.close();
            throw ex;
        }
    }

    public void close() {
        try {
            if (this.sock != null) {
                this.sock.close();
                this.sock = null;
            }
        } catch (IOException ex) {
        }
    }

    protected void finalize()
    {
        this.close();
    }
}
