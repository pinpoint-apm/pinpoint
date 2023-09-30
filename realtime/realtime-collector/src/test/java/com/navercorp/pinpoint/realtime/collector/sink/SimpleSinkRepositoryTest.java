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
package com.navercorp.pinpoint.realtime.collector.sink;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author youngjin.kim2
 */
public class SimpleSinkRepositoryTest {

    @Test
    public void test() {
        AtomicLong idCounter = new AtomicLong(0);
        SinkRepository<FluxSink<Integer>> sinkRepository = new SimpleSinkRepository<>(idCounter);
        int numProcessor = Runtime.getRuntime().availableProcessors();
        SinkRepositoryTest.test(sinkRepository, numProcessor, 10000);
    }

}
