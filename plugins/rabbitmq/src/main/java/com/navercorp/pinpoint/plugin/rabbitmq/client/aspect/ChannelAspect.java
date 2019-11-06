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
        AMQP.BasicProperties sourceProps = props;
        if (sourceProps == null) {
            sourceProps = MessageProperties.MINIMAL_BASIC;
        }
        AMQP.BasicProperties useProps = copy(sourceProps);
        __basicPublish(exchange, routingKey, mandatory, immediate, useProps, body);
    }

    private AMQP.BasicProperties copy(AMQP.BasicProperties source) {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.contentType(source.getContentType());
        builder.contentEncoding(source.getContentEncoding());
        builder.headers(source.getHeaders());
        builder.deliveryMode(source.getDeliveryMode());
        builder.priority(source.getPriority());
        builder.correlationId(source.getCorrelationId());
        builder.replyTo(source.getReplyTo());
        builder.expiration(source.getExpiration());
        builder.messageId(source.getMessageId());
        builder.timestamp(source.getTimestamp());
        builder.type(source.getType());
        builder.userId(source.getUserId());
        builder.appId(source.getAppId());
        builder.clusterId(source.getClusterId());
        return builder.build();
    }

    @JointPoint
    abstract void __basicPublish(String exchange, String routingKey,
                                 boolean mandatory, boolean immediate,
                                 AMQP.BasicProperties props, byte[] body);
}
