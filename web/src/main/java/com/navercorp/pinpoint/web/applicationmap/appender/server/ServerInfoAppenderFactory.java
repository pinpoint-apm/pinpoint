/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.appender.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author HyunGil Jeong
 */
@Component
public class ServerInfoAppenderFactory {

    private final Executor executor;

    @Autowired
    public ServerInfoAppenderFactory(@Qualifier("serverInfoAppendExecutor") Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public ServerInfoAppender create(ServerInstanceListFactory serverInstanceListFactory) {
        return new DefaultServerInfoAppender(serverInstanceListFactory, executor);
    }
}
