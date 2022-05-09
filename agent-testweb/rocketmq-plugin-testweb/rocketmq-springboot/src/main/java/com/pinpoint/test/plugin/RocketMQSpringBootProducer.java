/*
 * Copyright 2021 NAVER Corp.
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
package com.pinpoint.test.plugin;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author messi-gao
 */
@RestController
public class RocketMQSpringBootProducer {
    private static final Logger logger = LogManager.getLogger(RocketMQSpringBootProducer.class);
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @GetMapping("/template/send")
    public String templateSend() {
        String topic = "TopicTest";
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload("Hello, World!2222".getBytes(UTF8)).build());
        return "success";
    }

    @GetMapping("/template/sendAsync")
    public String templatesendAsync() {
        String topic = "TopicTest";
        rocketMQTemplate.asyncSend(topic, MessageBuilder.withPayload("Hello, World!2222".getBytes(UTF8)).build(),
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        logger.info("async onSucess SendResult={}", sendResult);
                    }

                    @Override
                    public void onException(Throwable e) {
                        logger.info("async onException", e);

                    }
                });
        return "success";
    }
}
