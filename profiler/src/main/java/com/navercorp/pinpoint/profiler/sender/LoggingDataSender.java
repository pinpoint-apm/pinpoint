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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 * @author netspider
 */
public class LoggingDataSender implements EnhancedDataSender {

    public static final DataSender DEFAULT_LOGGING_DATA_SENDER = new LoggingDataSender();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean send(TBase<?, ?> data) {
        logger.info("send tBase:{}", data);
        return true;
    }


    @Override
    public void stop() {
        logger.info("LoggingDataSender stop");
    }

    @Override
    public boolean request(TBase<?, ?> data) {
        logger.info("request tBase:{}", data);
        return true;
    }

    @Override
    public boolean request(TBase<?, ?> data, int retry) {
        logger.info("request tBase:{} retry:{}", data, retry);
        return false;
    }


    @Override
    public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
        logger.info("request tBase:{} FutureListener:{}", data, listener);
        return false;
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        logger.info("addReconnectEventListener eventListener:{}", eventListener);
        return false;
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        logger.info("removeReconnectEventListener eventListener:{}", eventListener);
        return false;
    }

}
