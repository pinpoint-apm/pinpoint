/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RejectedExecutionHandlerChain implements RejectedExecutionHandler {
    private final RejectedExecutionHandler[] handlerChain;

    private RejectedExecutionHandlerChain(List<RejectedExecutionHandler> handlerChain) {
        Objects.requireNonNull(handlerChain, "handlerChain");
        this.handlerChain = handlerChain.toArray(new RejectedExecutionHandler[0]);
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        for (RejectedExecutionHandler rejectedExecutionHandler : handlerChain) {
            rejectedExecutionHandler.rejectedExecution(r, executor);
        }
    }

    public static class Builder {
        private final List<RejectedExecutionHandler> chain = new ArrayList<>();

        public Builder() {
        }

        public void addRejectHandler(RejectedExecutionHandler handler) {
            Objects.requireNonNull(handler, "handler");
            this.chain.add(handler);
        }

        public RejectedExecutionHandler build() {
            return new RejectedExecutionHandlerChain(chain);
        }
    }
}
