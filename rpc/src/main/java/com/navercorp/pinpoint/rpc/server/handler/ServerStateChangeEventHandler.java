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

import com.navercorp.pinpoint.rpc.StateChangeEventListener;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.PinpointServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
public abstract class ServerStateChangeEventHandler implements StateChangeEventListener<PinpointServer> {

    public static final ServerStateChangeEventHandler DISABLED_INSTANCE = new DisabledHandler();

    public abstract void stateUpdated(PinpointServer pinpointSocket, SocketStateCode updatedStateCode) throws Exception;

    private static class DisabledHandler extends ServerStateChangeEventHandler {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public void stateUpdated(PinpointServer pinpointServer, SocketStateCode updatedStateCode) throws Exception {
            logger.info("stateUpdated(). pinpointServer:{}, updatedStateCode:{}", pinpointServer, updatedStateCode);
        }

    }

}
