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
import reactor.core.publisher.Sinks;

import java.util.Objects;

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
        Sinks.Many<LogDemand> sink = Sinks.many().replay().all();
        LogDemandAcceptor acceptor = demand -> sink.emitNext(demand, Sinks.EmitFailureHandler.FAIL_FAST);
        registerDemandAcceptor(fileKey, acceptor);
        return sink.asFlux().doFinally(t -> unregisterDemandAcceptor(fileKey, acceptor));
    }

    private void registerDemandAcceptor(FileKey fileKey, LogDemandAcceptor acceptor) {
        this.acceptorRepository.addAcceptor(fileKey, acceptor);
    }

    private void unregisterDemandAcceptor(FileKey fileKey, LogDemandAcceptor acceptor) {
        this.acceptorRepository.removeAcceptor(fileKey, acceptor);
    }

}
