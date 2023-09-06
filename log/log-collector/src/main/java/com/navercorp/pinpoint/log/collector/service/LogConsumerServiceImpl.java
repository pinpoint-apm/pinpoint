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
package com.navercorp.pinpoint.log.collector.service;

import com.navercorp.pinpoint.log.collector.repository.LogAcceptorRepository;
import com.navercorp.pinpoint.log.collector.repository.LogConsumer;
import com.navercorp.pinpoint.log.collector.repository.LogConsumerRepository;
import com.navercorp.pinpoint.log.collector.repository.LogDemandAcceptor;
import com.navercorp.pinpoint.log.dto.LogDemand;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.LogPile;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
class LogConsumerServiceImpl implements LogConsumerService {

    private final Scheduler scheduler;

    private final LogAcceptorRepository acceptorRepository;
    private final LogConsumerRepository consumerRepository;

    LogConsumerServiceImpl(
            Scheduler scheduler,
            LogAcceptorRepository acceptorRepository,
            LogConsumerRepository consumerRepository
    ) {
        this.acceptorRepository = Objects.requireNonNull(acceptorRepository, "acceptorRepository");
        this.consumerRepository = Objects.requireNonNull(consumerRepository, "consumerRepository");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    @Override
    public Flux<LogPile> tail(FileKey key, Duration duration) {
        return Flux.<LogPile>create(sink -> {
            LogConsumerImpl consumer = new LogConsumerImpl(key, sink::next);
            this.consumerRepository.addConsumer(consumer);
            request(key, duration);
            sink.onDispose(() -> this.consumerRepository.removeConsumer(consumer));
        }).take(duration, this.scheduler);
    }

    @Override
    public List<FileKey> getFileKeys() {
        return new ArrayList<>(this.acceptorRepository.getAcceptableKeys());
    }

    private void request(FileKey key, Duration duration) {
        LogDemand demand = new LogDemand(key, duration.toMillis());
        for (LogDemandAcceptor acceptor: this.acceptorRepository.getAcceptors(key)) {
            acceptor.accept(demand);
        }
    }

    private static class LogConsumerImpl implements LogConsumer {

        private final FileKey fileKey;
        private final Consumer<LogPile> sink;

        public LogConsumerImpl(FileKey fileKey, Consumer<LogPile> sink) {
            this.fileKey = fileKey;
            this.sink = sink;
        }

        @Override
        public void consume(LogPile pile) {
            this.sink.accept(pile);
        }

        @Override
        public FileKey getFileKey() {
            return this.fileKey;
        }

    }

}
