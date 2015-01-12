/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.server.handler;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocketStateCode;

/**
 * @author koo.taejin
 */
public abstract class ExecutionChannelStateChangeEventHandler implements ChannelStateChangeEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final ChannelStateChangeEventHandler handler;
    private final Executor executor;
    
    public ExecutionChannelStateChangeEventHandler(ChannelStateChangeEventHandler handler, Executor executor) {
        this.handler = handler;
        this.executor = executor;
    }
    
    @Override
    public void eventPerformed(ChannelContext channelContext, PinpointServerSocketStateCode stateCode) {
        logger.info("{} eventPerformed {}:{}", this.getClass().getSimpleName(), channelContext, stateCode);

        Execution execution = new Execution(channelContext, stateCode);
        this.executor.execute(execution);
    }
    
    private class Execution implements Runnable {
        private final ChannelContext channelContext;
        private final PinpointServerSocketStateCode stateCode;

        public Execution(ChannelContext channelContext, PinpointServerSocketStateCode stateCode) {
            this.channelContext = channelContext;
            this.stateCode = stateCode;
        }
        
        @Override
        public void run() {
            try {
                handler.eventPerformed(channelContext, stateCode);
            } catch (Exception e) {
                handler.exceptionCaught(channelContext, stateCode, e);
            }
        }
    }

}
