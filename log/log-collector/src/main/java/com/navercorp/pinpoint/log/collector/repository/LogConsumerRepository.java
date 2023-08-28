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
package com.navercorp.pinpoint.log.collector.repository;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.navercorp.pinpoint.log.vo.FileKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * @author youngjin.kim2
 */
public class LogConsumerRepository {

    private final SetMultimap<FileKey, LogConsumer> consumers = Multimaps.synchronizedSetMultimap(
            Multimaps.newSetMultimap(new HashMap<>(32), () -> new LinkedHashSet<>(2))
    );

    public LogConsumer getConsumer(FileKey key) {
        return getLast(this.consumers.get(key).iterator());
    }

    public void addConsumer(LogConsumer consumer) {
        this.consumers.put(consumer.getFileKey(), consumer);
    }

    public void removeConsumer(LogConsumer consumer) {
        this.consumers.remove(consumer.getFileKey(), consumer);
    }

    private static <T> T getLast(Iterator<T> iterator) {
        T item = null;
        while (iterator.hasNext()) {
            item = iterator.next();
        }
        return item;
    }

}
