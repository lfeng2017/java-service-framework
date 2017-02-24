package com.yongche.psf.spring;

import com.yongche.psf.server.ServerManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Created by stony on 16/11/5.
 */
public class ServerManagerBeanDefinitionParser extends AbstractBeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        Class<?> beanClass = ServerManager.class;
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        String id = element.getAttribute("name");
        String weight = element.getAttribute("weight");
        String ioThreads = element.getAttribute("io-threads");
        String serviceThreads = element.getAttribute("service-threads");
        if(isNotEmpty(weight)){
            beanDefinition.getPropertyValues().add("weight", weight);
        }
        if(isNotEmpty(ioThreads)){
            beanDefinition.getPropertyValues().add("ioThreads", ioThreads);
        }

        beanDefinition.setInitMethodName(element.getAttribute("init-method"));
        int port = Integer.valueOf(element.getAttribute("port"));
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, port);
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, element.getAttribute("service-type"));
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(2, element.getAttribute("version"));
        beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(3, new RuntimeBeanReference(element.getAttribute("service-center-manager")));
        if(isNotEmpty(serviceThreads)){
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(4, serviceThreads);
        }
        id = checkId(id, beanClass);
        if (parserContext.getRegistry().containsBeanDefinition(id))  {
            throw new IllegalStateException("Duplicate spring bean id " + id);
        }
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        return  beanDefinition;
    }
}
