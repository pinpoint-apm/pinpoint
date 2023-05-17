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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public interface PubSubServerFactory {

    <D, S> PubSubServer build(Function<D, Mono<S>> service, PubSubMonoServiceDescriptor<D, S> descriptor);

    <D, S> PubSubServer build(Function<D, Flux<S>> service, PubSubFluxServiceDescriptor<D, S> descriptor);

}
