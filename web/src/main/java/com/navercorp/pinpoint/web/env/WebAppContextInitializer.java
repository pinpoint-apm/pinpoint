package com.navercorp.pinpoint.web.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * for @ContextConfiguration(initializers)
 */
public class WebAppContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        logger.info("{} initialize", this.getClass().getSimpleName());

        EnvironmentPostProcessor webEnvironmentPostProcessor = new WebEnvironmentPostProcessor("local");

        ConfigurableEnvironment environment = configurableApplicationContext.getEnvironment();
        webEnvironmentPostProcessor.postProcessEnvironment(environment, null);


        ConfigurableListableBeanFactory beanFactory = configurableApplicationContext.getBeanFactory();

        addPropertySourcesPlaceholderConfigurer(beanFactory);
    }

    private void addPropertySourcesPlaceholderConfigurer(ConfigurableListableBeanFactory beanFactory) {
        try {
            beanFactory.getBean(PropertySourcesPlaceholderConfigurer.class);
            return;
        } catch (NoSuchBeanDefinitionException ignore) {
            // ignore
        }
        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;

            logger.debug("Add PropertySourcesPlaceholderConfigurer");

            GenericBeanDefinition bd = new GenericBeanDefinition();
            bd.setBeanClass(PropertySourcesPlaceholderConfigurer.class);
            beanDefinitionRegistry.registerBeanDefinition("propertySourcesPlaceholderConfigurer", bd);
        }
    }
}
