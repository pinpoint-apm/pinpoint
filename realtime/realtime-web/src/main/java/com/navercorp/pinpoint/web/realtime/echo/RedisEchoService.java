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
import com.navercorp.pinpoint.realtime.dto.Echo;
import com.navercorp.pinpoint.redis.value.Incrementer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class RedisEchoService {

    private static final Logger logger = LogManager.getLogger(RedisEchoService.class);

    private final Incrementer incrementer;
    private final EchoDao dao;

    public RedisEchoService(Incrementer incrementer, EchoDao dao) {
        this.incrementer = Objects.requireNonNull(incrementer, "incrementer");
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    public String echo(ClusterKey clusterKey, String message) {
        long id = this.incrementer.get();
        Echo echo = new Echo(id, clusterKey, message);
        Mono<Echo> res = this.dao.test(echo);

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
