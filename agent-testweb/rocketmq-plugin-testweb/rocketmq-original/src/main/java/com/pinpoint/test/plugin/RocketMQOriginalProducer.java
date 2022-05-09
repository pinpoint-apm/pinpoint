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

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @author messi-gao
 */
@RestController
public class RocketMQOriginalProducer {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(RocketMQOriginalProducer.class, args);
    }

    @Value("${namesrvAddr}")
    private String namesrvAddr;

    @Bean
    public DefaultMQProducer producer() throws MQClientException {
        DefaultMQProducer producer = new
                DefaultMQProducer("test");
        // Specify name server addresses.
        producer.setNamesrvAddr(namesrvAddr);
        //Launch the instance.
        producer.start();
        return producer;
    }

    @Autowired
    private DefaultMQProducer producer;

    @GetMapping("/original/send")
    public String send()
            throws UnsupportedEncodingException, RemotingException, MQClientException, InterruptedException,
                   MQBrokerException {
        Message msg = new Message("TopicTest",
                                  "TagA",
                                  "OrderID188",
                                  "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));
        SendResult send = producer.send(msg);
        logger.info(send.getMsgId());
        return "success";
    }

    @GetMapping("/original/sendAsync")
    public String sendAsync()
            throws UnsupportedEncodingException, RemotingException, MQClientException, InterruptedException {
        Message msg = new Message("TopicTest",
                                  "TagA",
                                  "OrderID188",
                                  "Hello world".getBytes(RemotingHelper.DEFAULT_CHARSET));
        producer.send(msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info(sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                logger.info("Exception", e);
            }
        });
        return "success";
    }
}
