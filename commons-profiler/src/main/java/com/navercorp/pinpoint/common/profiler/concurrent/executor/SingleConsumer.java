/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.concurrent.executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SingleConsumer<T> implements MultiConsumer<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Consumer<T> consumer;

    public SingleConsumer(Consumer<T> consumer) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public void acceptN(Collection<T> messageList) {
        // Cannot use toArray(T[] array) because passed messageList doesn't implement it properly.
        Object[] dataList = messageList.toArray();

        // No need to copy because this runs with single thread.
        // Object[] copy = Arrays.copyOf(original, original.length);

        final int size = messageList.size();
        for (int i = 0; i < size; i++) {
            try {
                this.accept((T) dataList[i]);
            } catch (Throwable th) {
                logger.warn("Unexpected Error. Cause:{}", th.getMessage(), th);
            }
        }
    }

    public void accept(T message) {
        this.consumer.accept(message);
    }

}
