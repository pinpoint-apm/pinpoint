/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.flink.receiver;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author minwoo.jung
 */
public abstract class SourceContextManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<SourceFunction.SourceContext> sourceContextList = new CopyOnWriteArrayList<>();
    private AtomicInteger callCount = new AtomicInteger(1);

    public void addSourceContext(SourceFunction.SourceContext sourceContext) {
        logger.info("add sourceContext.");
        sourceContextList.add(sourceContext);
    }

    protected SourceFunction.SourceContext roundRobinSourceContext() {
        if (sourceContextList.isEmpty()) {
            logger.warn("sourceContextList is empty.");
            return null;
        }

        int count = callCount.getAndIncrement();
        int sourceContextListIndex = count % sourceContextList.size();

        if (sourceContextListIndex < 0) {
            sourceContextListIndex = sourceContextListIndex * -1;
            callCount.set(0);
        }

        try {
            return sourceContextList.get(sourceContextListIndex);
        } catch (Exception e) {
            logger.warn("not get sourceContext", e);
        }

        return null;
    }
}
