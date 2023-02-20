/*
 * Copyright 2023 NAVER Corp.
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
 */
package com.navercorp.pinpoint.redis;

import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.pubsub.SubConsumer;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class StaticSubscriber<T> implements InitializingBean {

    private final SubChannel<T> channel;
    private final SubConsumer<T> consumer;
    private final String postfix;

    public StaticSubscriber(
            SubChannel<T> channel,
            SubConsumer<T> consumer,
            String postfix
    ) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.postfix = postfix;
    }

    @Override
    public void afterPropertiesSet() {
        this.channel.subscribe(this.consumer, this.postfix);
    }

}
