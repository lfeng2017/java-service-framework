package com.yongche.psf.spring;

import com.yongche.psf.service.ServiceController;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Set;

/**
 * Created by stony on 16/11/5.
 */
public class ServiceScanBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String basePackage = element.getAttribute("base-package");
        basePackage = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(basePackage);
        String[] basePackages = StringUtils.tokenizeToStringArray(basePackage, ",; \t\n");
        ServiceBeanScanner scanner = new ServiceBeanScanner(parserContext.getReaderContext().getRegistry());
        scanner.scan(basePackages);
        return null;
    }

    class ServiceBeanScanner extends ClassPathBeanDefinitionScanner{
        private volatile boolean isInit = false;
        private final Object monitor = new Object();
        /**
         * 不加载默认的过滤器
         * @param registry
         */
        public ServiceBeanScanner(BeanDefinitionRegistry registry) {
            this(registry, false);
        }

        public ServiceBeanScanner(BeanDefinitionRegistry registry, boolean b) {
            super(registry, b);
            init();
        }
        public void init(){
            if(isInit){
                return;
            }
            setIncludeAnnotationConfig(true);
            addIncludeFilter(new AnnotationTypeFilter(ServiceController.class));
            addExcludeFilter(new TypeFilter() {
                @Override
                public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                    String className = metadataReader.getClassMetadata().getClassName();
                    return className.endsWith("package-info");
                }
            });
            synchronized (this.monitor) {
                this.isInit = true;
            }
        }
    }
}
