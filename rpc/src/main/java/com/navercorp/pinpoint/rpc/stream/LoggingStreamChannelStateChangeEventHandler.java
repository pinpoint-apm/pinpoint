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

package com.navercorp.pinpoint.rpc.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Taejin Koo
 */
public class LoggingStreamChannelStateChangeEventHandler implements StreamChannelStateChangeEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void eventPerformed(StreamChannel streamChannel, StreamChannelStateCode oldStateCode, StreamChannelStateCode updatedStateCode) throws Exception {
        logger.info(createMessage(true, streamChannel, oldStateCode, updatedStateCode));
    }

    @Override
    public void exceptionCaught(StreamChannel streamChannel, StreamChannelStateCode oldStateCode, StreamChannelStateCode updatedStateCode, Throwable e) {
        logger.info("{} message={}", createMessage(false, streamChannel, oldStateCode, updatedStateCode), e.getMessage(), e);
    }

    private String createMessage(boolean isSuccess, StreamChannel streamChannel, StreamChannelStateCode oldState, StreamChannelStateCode updatedState) {
        StringBuilder message = new StringBuilder(32);
        message.append("Change state to ");
        message.append(oldState).append("->").append(updatedState);
        message.append("(").append(isSuccess ? "SUCCESS" : "FAIL").append(")");
        message.append(" [Channel:").append(streamChannel).append("] ");
        return message.toString();
    }

}
