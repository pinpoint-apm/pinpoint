/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pinpoint.test.plugin.RocketMQSpringCloudStreamProducer.MySource;

/**
 * @author messi-gao
 */
@RestController
@EnableBinding({ MySource.class })
public class RocketMQSpringCloudStreamProducer {
    @Autowired
    private MySource source;

    @GetMapping("/stream/send")
    public String sendWithTags() throws Exception {
        Message message = MessageBuilder
                .createMessage("hello rocketmq stream", new MessageHeaders(Stream.of("test").collect(Collectors
                                                                                                             .toMap(str -> MessageConst.PROPERTY_TAGS,
                                                                                                                    String::toString))));
        source.output1().send(message);
        return "success";
    }

    public interface MySource {
        @Output("output1")
        MessageChannel output1();
    }
}
