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

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
@Component
public class ServerInfoAppenderFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String mode;
    private final ExecutorService executorService;

    @Autowired
    public ServerInfoAppenderFactory(
            @Value("#{pinpointWebProps['web.servermap.appender.mode'] ?: 'serial'}") String mode,
            @Value("#{pinpointWebProps['web.servermap.appender.parallel.maxthreads'] ?: 16}") int maxThreads) {
        logger.info("ServerInfoAppender mode : {}", mode);
        this.mode = mode;
        if (this.mode.equalsIgnoreCase("parallel")) {
            executorService = Executors.newFixedThreadPool(maxThreads, new PinpointThreadFactory("Pinpoint-node-histogram-appender", true));
        } else {
            executorService = null;
        }
    }

    public ServerInfoAppender create(ServerInstanceListFactory serverInstanceListFactory) {
        if (mode.equalsIgnoreCase("parallel")) {
            return new ParallelServerInfoAppender(serverInstanceListFactory, executorService);
        }
        return new SerialServerInfoAppender(serverInstanceListFactory);
    }

    @PreDestroy
    public void preDestroy() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
