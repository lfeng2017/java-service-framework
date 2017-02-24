package com.yongche.psf.container;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by stony on 16/11/9.
 */
public class Main {

    private static volatile boolean running = true;
    public static void main(String[] args){
        final Container container;
        try {
            container = new SpringContainer();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        container.stop();
                        System.out.println("PSF server stopped!");
                    } catch (Throwable t) {
                        System.out.println(t.getMessage());
                    }
                    synchronized (Main.class) {
                        running = false;
                        Main.class.notify();
                    }
                }
            });
            container.start();
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " PSF service server started!");
        } catch (RuntimeException e) {
            running = false;
            e.printStackTrace();
            System.exit(1);
        }
        synchronized (Main.class) {
            while (running) {
                try {
                    Main.class.wait();
                } catch (Throwable e) {
                }
            }
        }
    }
}
