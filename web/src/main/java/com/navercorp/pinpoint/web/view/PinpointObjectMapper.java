/**
 * Copyright 2014 NAVER Corp.
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
 */
package com.navercorp.pinpoint.web.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.converter.json.SpringHandlerInstantiator;

/**
 * @author Jongho Moon
 *
 */
public class PinpointObjectMapper extends ObjectMapper implements InitializingBean, BeanFactoryAware {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BeanFactory beanFactory;
    @Override
    public void afterPropertiesSet() throws Exception {
        registerModule(new JacksonPinpointModule());
        if (beanFactory != null) {
            if (beanFactory instanceof AutowireCapableBeanFactory) {
                logger.debug("PinpointObjectMapper.setSpringHandlerInstantiator");
                final SpringHandlerInstantiator hi = new SpringHandlerInstantiator((AutowireCapableBeanFactory) beanFactory);
                this.setHandlerInstantiator(hi);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
