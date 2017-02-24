package com.yongche.psf.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;

/**
 * Created by stony on 16/11/5.
 */
public class PSFNamespaceHandler extends NamespaceHandlerSupport {


    public PSFNamespaceHandler() {
    }

    @Override
    public void init() {
        //service-scan
        this.registerBeanDefinitionParser("service-scan", new ServiceScanBeanDefinitionParser());
        this.registerBeanDefinitionParser("service-center-manager", new ServiceCenterManagerBeanDefinitionParser());
        this.registerBeanDefinitionParser("server-manager", new ServerManagerBeanDefinitionParser());
        this.registerBeanDefinitionParser("client-manager", new ClientManagerBeanDefinitionParser());
    }
}
