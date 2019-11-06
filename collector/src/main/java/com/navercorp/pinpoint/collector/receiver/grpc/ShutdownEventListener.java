/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ShutdownEventListener implements ApplicationListener<ContextClosedEvent> {

    private volatile boolean shutdown;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent) {
        final Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("onApplicationEvent:{}", contextClosedEvent);
        shutdown = true;
    }


    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public String toString() {
        return "ShutdownEventListener{" +
                "shutdown=" + shutdown +
                '}';
    }
}
