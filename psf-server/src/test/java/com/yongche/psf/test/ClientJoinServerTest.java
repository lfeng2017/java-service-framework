package com.yongche.psf.test;

import com.yongche.psf.core.ConnectionInfo;
import com.yongche.psf.core.PackageBuilder;
import com.yongche.psf.core.ProtocolHeader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Created by stony on 16/11/3.
 */
public class ClientJoinServerTest {

    private ConnectionInfo connectionInfo;
    private PackageBuilder packageBuilder;
    private OutputStream out;
    private InputStream in;

    public ClientJoinServerTest() throws Exception {
        connectionInfo = new ConnectionInfo("127.0.0.1", 28080);
        packageBuilder = new PackageBuilder();
        connectionInfo.connect(packageBuilder.context.connect_timeout,packageBuilder.context.network_timeout);

        join();
    }

    private void join() throws Exception{
        ProtocolHeader header;
        try {
            int len = packageBuilder.buildClientJoinPackage("weather");

            out = connectionInfo.sock.getOutputStream();
            out.write(packageBuilder.context.send_recv_buf, 0, len);
            out.flush();

            in = connectionInfo.sock.getInputStream();
            header = packageBuilder.recvHeader(in);
            if (header.status != 0) {
                throw new IOException("recv header status != 0 from server center, join server error.");
            }
            System.out.println("join server :28080 success " + header);
        }catch (IOException e){
            connectionInfo.close();
            throw e;
        }
    }


    @Test
    public void test() throws Exception {
        ClientJoinServerTest test = new ClientJoinServerTest();
    }


}
