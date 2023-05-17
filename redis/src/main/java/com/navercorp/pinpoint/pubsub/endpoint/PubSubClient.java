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

import java.util.Objects;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
class PubSubClient<D, S> {

    protected final PubSubClientOptions options;
    protected final IdentifierFactory identifierFactory;
    protected final Function<Identifier, PubChannel<DemandMessage<D>>> demandRouter;
    protected final Function<Identifier, SubChannel<SupplyMessage<S>>> supplyRouter;

    PubSubClient(
            PubSubClientOptions options,
            IdentifierFactory identifierFactory,
            Function<Identifier, PubChannel<DemandMessage<D>>> demandRouter,
            Function<Identifier, SubChannel<SupplyMessage<S>>> supplyRouter
    ) {
        this.options = Objects.requireNonNull(options, "options");
        this.identifierFactory = Objects.requireNonNull(identifierFactory, "demandIdentifierFactory");
        this.demandRouter = Objects.requireNonNull(demandRouter, "demandRouter");
        this.supplyRouter = Objects.requireNonNull(supplyRouter, "supplyRouter");
    }

}
