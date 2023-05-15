/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.rpc.ResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;


/**
 * @author emeroad
 * @author netspider
 */
public class LoggingDataSender<T> implements EnhancedDataSender<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public boolean send(T data) {
        logger.info("send tBase:{}", data);
        return true;
    }


    @Override
    public void stop() {
        logger.info("LoggingDataSender stop");
    }

    @Override
    public boolean request(T data) {
        logger.info("request tBase:{}", data);
        return true;
    }

    @Override
    public boolean request(T data, int retry) {
        logger.info("request tBase:{} retry:{}", data, retry);
        return false;
    }


    @Override
    public boolean request(T data, BiConsumer<ResponseMessage, Throwable> listener) {
        logger.info("request tBase:{} FutureListener:{}", data, listener);
        return false;
    }

}
