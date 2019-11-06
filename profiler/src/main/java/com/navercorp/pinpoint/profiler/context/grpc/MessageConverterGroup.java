/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MessageConverterGroup<Message> implements MessageConverter<Message> {

    private final MessageConverter<Message>[] group;

    public static <Message> MessageConverterGroup<Message> wrap(MessageConverter<Message>... group) {
        return new MessageConverterGroup<Message>(group);
    }

    private MessageConverterGroup(MessageConverter<Message>[] group) {
        Assert.requireNonNull(group, "list");
        this.group = Arrays.copyOf(group, group.length);
    }


    @Override
    public Message toMessage(Object message) {
        for (MessageConverter<Message> vMessageConverter : group) {
            final Message convertedMessage = vMessageConverter.toMessage(message);
            if (convertedMessage != null) {
                return convertedMessage;
            }
        }
        return null;
    }
}
