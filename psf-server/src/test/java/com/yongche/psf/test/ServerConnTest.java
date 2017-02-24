package com.yongche.psf.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by stony on 16/11/2.
 */
public class ServerConnTest {

    public static void main(String[] args) throws IOException {
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress("127.0.0.1", 20881), 3000);

        OutputStream out =  sock.getOutputStream();

        byte[] buff = "connection".getBytes("UTF-8");

        out.write(buff);

        InputStream in = sock.getInputStream();

        byte[] recBuff = new byte[buff.length];
        in.read(recBuff,0,buff.length);
        System.out.println(recBuff);
        System.out.println(new String(recBuff,"UTF-8"));
    }
}
