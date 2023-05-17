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
package com.navercorp.pinpoint.pubsub.endpoint;

import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.pubsub.SubChannel;
import org.springframework.beans.factory.InitializingBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public interface PubSubServer extends InitializingBean {

    void start();

    default void afterPropertiesSet() {
        start();
    }

    static <D, S> PubSubServer buildLongTerm(
            Function<D, Flux<S>> service,
            SubChannel<DemandMessage<D>> demandChannel,
            Function<Identifier, PubChannel<SupplyMessage<S>>> supplyRouter
    ) {
        return new PubSubServerImpl<>(null, service, demandChannel, supplyRouter);
    }

    static <D, S> PubSubServer build(
            Function<D, Mono<S>> service,
            SubChannel<DemandMessage<D>> demandChannel,
            Function<Identifier, PubChannel<SupplyMessage<S>>> supplyRouter
    ) {
        return new PubSubServerImpl<>(service, null, demandChannel, supplyRouter);
    }

}
