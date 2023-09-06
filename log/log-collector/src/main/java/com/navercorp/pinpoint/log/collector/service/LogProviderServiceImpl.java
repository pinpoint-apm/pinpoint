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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
class LogProviderServiceImpl implements LogProviderService {

    private final LogAcceptorRepository acceptorRepository;
    private final LogConsumerRepository consumerRepository;

    LogProviderServiceImpl(LogAcceptorRepository acceptorRepository, LogConsumerRepository consumerRepository) {
        this.acceptorRepository = Objects.requireNonNull(acceptorRepository, "acceptorRepository");
        this.consumerRepository = Objects.requireNonNull(consumerRepository, "consumerRepository");
    }

    @Override
    public void provide(FileKey fileKey, LogPile pile) {
        LogConsumer consumer = this.consumerRepository.getConsumer(fileKey);
        if (consumer != null) {
            consumer.consume(pile);
        }
    }

    @Override
    public Flux<LogDemand> getDemands(FileKey fileKey) {
        return Flux.create(sink -> {
            LogDemandAcceptor acceptor = new LogDemanderAcceptorImpl(fileKey, sink::next);
            this.acceptorRepository.addAcceptor(acceptor);
            sink.onDispose(() -> this.acceptorRepository.removeAcceptor(acceptor));
        });
    }

    private static class LogDemanderAcceptorImpl implements LogDemandAcceptor {

        private final FileKey fileKey;
        private final Consumer<LogDemand> sink;

        public LogDemanderAcceptorImpl(FileKey fileKey, Consumer<LogDemand> sink) {
            this.fileKey = fileKey;
            this.sink = sink;
        }

        @Override
        public void accept(LogDemand demand) {
            this.sink.accept(demand);
        }

        @Override
        public FileKey getFileKey() {
            return this.fileKey;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

    }

}
