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
package com.navercorp.pinpoint.web.realtime.echo;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoClient;
import com.navercorp.pinpoint.realtime.dto.Echo;
import com.navercorp.pinpoint.web.service.EchoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class EchoServiceImpl implements EchoService {

    private static final Logger logger = LogManager.getLogger(EchoServiceImpl.class);

    private final PubSubMonoClient<Echo, Echo> echoEndpoint;

    EchoServiceImpl(PubSubMonoClient<Echo, Echo> echoEndpoint) {
        this.echoEndpoint = Objects.requireNonNull(echoEndpoint, "echoEndpoint");
    }

    @Override
    public String echo(ClusterKey clusterKey, String message) {
        final Echo echo = new Echo(clusterKey, message);
        final Mono<Echo> res = this.echoEndpoint.request(echo);

        try {
            final Echo result = res.block();
            if (result != null) {
                return result.getMessage();
            }
        } catch (Exception e) {
            logger.error("Failed to getSession echo result", e);
            throw new RuntimeException("Failed to getSession echo result", e);
        }

        logger.error("Failed to getSession echo result");
        throw new RuntimeException("Failed to getSession echo result");
    }

}
