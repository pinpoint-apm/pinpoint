/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BulkOperationReporterFactory implements BeanDefinitionRegistryPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConfigurableListableBeanFactory beanFactory;
    private BeanDefinitionRegistry registry;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public BulkOperationReporter getBulkOperationReporter(String name) {
        Objects.requireNonNull(name, "name");

        if (registry.containsBeanDefinition(name)) {
            return getReporter(name);
        }
        logger.info("registerBeanDefinition:{} {}", name, BulkOperationReporter.class);

        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(BulkOperationReporter.class);
        this.registry.registerBeanDefinition(name, bd);

        return getReporter(name);
    }

    private BulkOperationReporter getReporter(String name) {
        return beanFactory.getBean(name, BulkOperationReporter.class);
    }

}
