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
package com.navercorp.pinpoint.realtime.collector.echo.service;

import com.navercorp.pinpoint.realtime.collector.service.AgentCommandService;
import com.navercorp.pinpoint.realtime.dto.Echo;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import org.apache.thrift.TBase;
import reactor.core.publisher.Mono;

/**
 * @author youngjin.kim2
 */
class EchoServiceImpl implements EchoService {

    private final AgentCommandService commandService;

    EchoServiceImpl(AgentCommandService commandService) {
        this.commandService = commandService;
    }

    @Override
    public Mono<Echo> echo(Echo echo) {
        final TCommandEcho command = new TCommandEcho(echo.getMessage());
        final Mono<TBase<?, ?>> response = this.commandService.request(echo.getAgentKey(), command);
        if (response == null) {
            return null;
        }
        return response
                .flatMap(EchoServiceImpl::compose)
                .map(message -> new Echo(echo.getAgentKey(), message));
    }

    private static Mono<String> compose(TBase<?, ?> res) {
        if (res instanceof TCommandEcho) {
            final TCommandEcho echo = (TCommandEcho) res;
            return Mono.just(echo.getMessage());
        }
        return Mono.empty();
    }

}
