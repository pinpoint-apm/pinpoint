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
package com.navercorp.pinpoint.web.realtime;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.realtime.echo.RedisEchoService;
import com.navercorp.pinpoint.web.service.EchoService;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class RedisEchoServiceAdaptor implements EchoService {

    private final RedisEchoService delegate;

    public RedisEchoServiceAdaptor(RedisEchoService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public String echo(ClusterKey clusterKey, String message) {
        return this.delegate.echo(clusterKey, message);
    }

}
