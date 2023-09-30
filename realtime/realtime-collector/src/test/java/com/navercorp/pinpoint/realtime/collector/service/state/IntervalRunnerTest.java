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
package com.navercorp.pinpoint.realtime.collector.service.state;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class IntervalRunnerTest {

    @Test
    public void test() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        IntervalRunner runner = new IntervalRunner(() -> {
            counter.getAndIncrement();
        }, Duration.ofMillis(10), Schedulers.boundedElastic());
        runner.afterPropertiesSet();
        Mono.delay(Duration.ofMillis(100)).block();
        runner.destroy();
        Integer result = counter.get();
        assertThat(result).isGreaterThan(1).isLessThan(100);
    }

}
