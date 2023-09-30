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

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
public class SinkRepositoryTest {

    static void test(SinkRepository<FluxSink<Integer>> sinkRepository, int numWorker, int numWork) {
        ExecutorService executor = Executors.newWorkStealingPool(numWorker);
        for (int i = 0; i < numWork; i++) {
            executor.submit(() -> {
                SinkRepositoryTest.test(sinkRepository);
            });
        }
    }

    static void test(SinkRepository<FluxSink<Integer>> sinkRepository) {
        Flux<Integer> flux = Flux.create(sink -> {
            long sinkId = sinkRepository.put(sink);
            Mono.delay(Duration.ofMillis(1))
                    .publishOn(Schedulers.boundedElastic())
                    .subscribe(t -> provide(sinkRepository, sinkId));
            sink.onDispose(() -> sinkRepository.invalidate(sinkId));
        });
        List<Integer> result = flux.collectList().block();
        assertThat(result).hasSameElementsAs(List.of(0, 1, 2, 3, 4));
    }

    private static void provide(SinkRepository<FluxSink<Integer>> sinkRepository, long sinkId) {
        FluxSink<Integer> sink = sinkRepository.get(sinkId);
        for (int i = 0; i < 5; i++) {
            sink.next(i);
        }
        sink.complete();
    }

}
