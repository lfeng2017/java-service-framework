package com.yongche.psf.spring;

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.util.StringUtils;

import static com.yongche.psf.core.PackageBuilder.minuscules;

/**
 * Created by stony on 16/11/11.
 */
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {



    protected String checkId(String id, Class<?> beanClass){
        if(!StringUtils.hasText(id)){
            id = minuscules(beanClass.getSimpleName());
        }
        return id;
    }

    protected boolean isEmpty(String v){
        return v == null || v.length() == 0;
    }

    protected boolean isNotEmpty(String v){
        return !isEmpty(v);
    }
}
