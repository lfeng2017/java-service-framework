package com.yongche.psf.core;

/**
 * Created by stony on 16/11/16.
 */
public abstract class LoggerHelper {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("com.yongche.psf");

    public static void info(String msg){
        logger.info(msg);
    }
    public static void info(String msg, Object...args){
        logger.info(msg, args);
    }

    public static void debug(String msg){
        logger.debug(msg);
    }
    public static void debug(String msg, Object...args){
        logger.debug(msg, args);
    }

    public static void warn(String msg){
        logger.warn(msg);
    }
    public static void warn(String msg, Object...args){
        logger.warn(msg, args);
    }
    public static void error(String msg){
        logger.error(msg);
    }
    public static void error(String msg, Throwable e){
        logger.error(msg, e);
    }

}
