/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rabbitmq.client.aspect;

import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;

/**
 * Make a copy of {@code AMQP.BasicProperties} to inject pinpoint headers.
 *
 * @author HyunGil Jeong
 */
@Aspect
public abstract class ChannelAspect {
    @PointCut
    public void basicPublish(String exchange, String routingKey,
                             boolean mandatory, boolean immediate,
                             AMQP.BasicProperties props, byte[] body) {
        // Use a separate Java class file.
        // For example, use local variables or use features that depend on Java versions such as lambdas.
        AMQP.BasicProperties useProps = BasicPropertiesHelper.copy(props);
        __basicPublish(exchange, routingKey, mandatory, immediate, useProps, body);
    }

    @JointPoint
    abstract void __basicPublish(String exchange, String routingKey,
                                 boolean mandatory, boolean immediate,
                                 AMQP.BasicProperties props, byte[] body);
}
