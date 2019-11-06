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

package com.navercorp.pinpoint.profiler.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class DefaultAsyncQueueingExecutorListener implements AsyncQueueingExecutorListener<Object> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(Collection<Object> messageList) {
        // Cannot use toArray(T[] array) because passed messageList doesn't implement it properly.
        Object[] dataList = messageList.toArray();

        // No need to copy because this runs with single thread.
        // Object[] copy = Arrays.copyOf(original, original.length);

        final int size = messageList.size();
        for (int i = 0; i < size; i++) {
            try {
                execute(dataList[i]);
            } catch (Throwable th) {
                logger.warn("Unexpected Error. Cause:{}", th.getMessage(), th);
            }
        }
    }


    public abstract void execute(Object message);

}
