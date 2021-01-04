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

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author messi-gao
 */
@RestController
public class RocketMQSpringBootProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("/template/send")
    public String templateSend() {
        String topic = "TopicTest";
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload("Hello, World!2222".getBytes()).build());
        return "success";
    }

    @GetMapping("/template/sendAsync")
    public String templatesendAsync() {
        String topic = "TopicTest";
        rocketMQTemplate.asyncSend(topic, MessageBuilder.withPayload("Hello, World!2222".getBytes()).build(),
                                   new SendCallback() {
                                       @Override
                                       public void onSuccess(SendResult sendResult) {
                                           System.out.printf("async onSucess SendResult=%s %n", sendResult);
                                       }

                                       @Override
                                       public void onException(Throwable e) {
                                           System.out.printf("async onException Throwable=%s %n", e);

                                       }
                                   });
        return "success";
    }
}
