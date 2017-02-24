package com.yongche.psf.server;

import com.yongche.psf.core.LoggerHelper;
import com.yongche.psf.core.NamedThreadFactory;
import com.yongche.psf.service.ServiceController;
import com.yongche.psf.service.ServiceMapping;
import com.yongche.psf.service.ServiceMappingInfo;
import com.yongche.psf.service.ServiceMappingResolve;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static com.yongche.psf.core.ContextHolder.DEFAULT_IO_THREAD;
import static com.yongche.psf.core.ContextHolder.DEFAULT_SERVICE_THREAD;

/**
 * server 管理器
 * Created by stony on 16/11/4.
 */
public class ServerManager implements ApplicationContextAware {

    int port;
    String serviceType;
    String version;
    String[] serviceCenter;
    Server server;
    int ioThreads = DEFAULT_IO_THREAD;
    int serviceThreads;
    final ThreadPoolExecutor serviceExecutor;

    Map<String, ServiceMappingInfo> urlMappings = new HashMap<>();

    public ServerManager(int port, String serviceType, String version, ServiceCenterManager serviceCenterManager) {
        this(port,serviceType,version,serviceCenterManager,DEFAULT_SERVICE_THREAD);
    }
    public ServerManager(int port, String serviceType, String version, ServiceCenterManager serviceCenterManager, int serviceThreads) {
        this.port = port;
        this.serviceType = serviceType;
        this.version = version;
        this.serviceCenter = serviceCenterManager.getServiceCenter();
        this.serviceThreads = serviceThreads;
        this.serviceExecutor = NamedThreadFactory.newExecutor(serviceThreads, -1);
    }

    public void init() {
        try {
            server = new Server(port,serviceType, version, serviceCenter, urlMappings, serviceExecutor);
            if(ioThreads != DEFAULT_IO_THREAD){
                server.setIoThreads(ioThreads);
            }
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ServiceMappingResolve serviceMappingResolve = new ServiceMappingResolve.ReflectionServiceMappingResolve();
        serviceMappingResolve.resolve(applicationContext, serviceType, urlMappings);
        LoggerHelper.debug(">> uriMappings {}", urlMappings);
    }

    public void registerMapping(String uri, ServiceMappingInfo info) {
        urlMappings.put(uri, info);
    }

    public void setWeight(short weight) {
        ServerMonitor.getInstance().setWeight(weight);
    }

    public void setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
    }


    /**
     * 释放资源
     */
    public void destroy(){
        if(server != null) server.close();
        urlMappings = null;
    }
}