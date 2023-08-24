/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.rabbitmq.spring;

import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.TestMessageHolder;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.service.TestReceiverService;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.service.TestSenderService;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author HyunGil Jeong
 */
public class TestApplicationContext {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    public void init(Class<?>... annotatedClasses) {
        context.register(annotatedClasses);
        context.refresh();
    }

    public void close() {
        context.close();
    }

    public ConnectionFactory getConnectionFactory() {
        return (ConnectionFactory) context.getBean("connectionFactory");
    }

    public TestMessageHolder getTestMessageHolder() {
        return (TestMessageHolder) context.getBean("testMessageHolder");
    }

    public TestSenderService getTestSenderService() {
        return (TestSenderService) context.getBean("testSenderService");
    }

    public TestReceiverService getTestReceiverService() {
        return (TestReceiverService) context.getBean("testReceiverService");
    }

    public RabbitTemplate getRabbitTemplate() {
        return (RabbitTemplate) context.getBean("rabbitTemplate");
    }
}
