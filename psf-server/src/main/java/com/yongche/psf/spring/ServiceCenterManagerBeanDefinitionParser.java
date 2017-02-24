package com.yongche.psf.spring;

import com.yongche.psf.server.ServiceCenterManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Created by stony on 16/11/5.
 */
public class ServiceCenterManagerBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        Class<?> beanClass = ServiceCenterManager.class;
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);
        String id = element.getAttribute("name");
        String serviceCentimes = element.getAttribute("service-center");
        if(!StringUtils.hasText(id)){
            id = beanClass.getSimpleName();
        }
        if (id != null && id.length() > 0) {
            if (parserContext.getRegistry().containsBeanDefinition(id))  {
                throw new IllegalStateException("Duplicate spring bean id " + id);
            }
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, serviceCentimes);
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        }
        return  beanDefinition;
    }
}
