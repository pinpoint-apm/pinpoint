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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.SubscribableChannel;

import com.pinpoint.test.plugin.RocketMQSpringCloudStreamConsumer.MySink;

/**
 * @author messi-gao
 */
@EnableBinding({ MySink.class })
public class RocketMQSpringCloudStreamConsumer {
    private final Logger logger = LogManager.getLogger(this.getClass());
    @StreamListener("input1")
    public void receiveInput1(String receiveMsg) {
        logger.info("input1 receive:{}");
    }

    public interface MySink {
        @Input("input1")
        SubscribableChannel input1();
    }
}
