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

package com.navercorp.pinpoint.grpc.server.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LifecycleListenerAdaptor implements LifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void connect(PingSession lifecycle) {
        logger.info("connect:{}", lifecycle);
    }

    @Override
    public void handshake(PingSession lifecycle) {
        logger.info("handshake {}", lifecycle);
    }

    @Override
    public void close(PingSession lifecycle) {
        logger.info("close:{}", lifecycle);
    }
}
