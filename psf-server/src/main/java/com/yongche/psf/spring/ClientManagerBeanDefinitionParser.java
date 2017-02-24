package com.yongche.psf.spring;

import com.yongche.psf.exception.ClientException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Created by stony on 16/11/11.
 */
public class ClientManagerBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        try {
            Class<?> beanClass = Class.forName("com.yongche.psf.client.ClientManager");
            RootBeanDefinition beanDefinition = new RootBeanDefinition();
            beanDefinition.setBeanClass(beanClass);
            beanDefinition.setLazyInit(false);
            String id = element.getAttribute("name");
            String clientModel = element.getAttribute("client-model");
            beanDefinition.setDestroyMethodName(element.getAttribute("destroy-method"));

            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, element.getAttribute("service-type"));
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, element.getAttribute("version"));
            beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(2, new RuntimeBeanReference(element.getAttribute("service-center-manager")));
            if(isNotEmpty(clientModel)){
                String clientModelBeanName = "clientModelBeanName";
                RootBeanDefinition clientModelBean = new RootBeanDefinition();
                clientModelBean.setBeanClass(org.springframework.beans.factory.config.FieldRetrievingFactoryBean.class);
                clientModelBean.setLazyInit(false);
                if("OIO".equals(clientModel)){
                    clientModelBean.getPropertyValues().add("staticField","com.yongche.psf.client.AbstractClient.ClientModel.OIO");
                }else if("NIO".equals(clientModel)){
                    clientModelBean.getPropertyValues().add("staticField","com.yongche.psf.client.AbstractClient.ClientModel.NIO");
                }
                parserContext.getRegistry().registerBeanDefinition(clientModelBeanName, clientModelBean);
                beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(3, new RuntimeBeanReference(clientModelBeanName));
            }
            id = checkId(id, beanClass);
            if (parserContext.getRegistry().containsBeanDefinition(id))  {
                throw new IllegalStateException("Duplicate spring bean id " + id);
            }
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);

            return  beanDefinition;
        } catch (ClassNotFoundException e) {
            throw new ClientException(e);
        }
    }
}
