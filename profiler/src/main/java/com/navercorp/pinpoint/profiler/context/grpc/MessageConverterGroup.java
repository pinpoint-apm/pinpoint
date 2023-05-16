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

import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MessageConverterGroup<IN, OUT> implements MessageConverter<IN, OUT> {

    private final MessageConverter<IN, OUT>[] group;

    public static <IN, OUT> MessageConverterGroup<IN, OUT> wrap(List<MessageConverter<IN, OUT>> group) {
        return new MessageConverterGroup<>(group);
    }

    private MessageConverterGroup(List<MessageConverter<IN, OUT>> group) {
        Objects.requireNonNull(group, "list");
        this.group = group.toArray(new MessageConverter[0]);
    }


    @Override
    public OUT toMessage(IN message) {
        for (MessageConverter<IN, OUT> vMessageConverter : group) {
            final OUT convertedMessage = vMessageConverter.toMessage(message);
            if (convertedMessage != null) {
                return convertedMessage;
            }
        }
        return null;
    }
}
