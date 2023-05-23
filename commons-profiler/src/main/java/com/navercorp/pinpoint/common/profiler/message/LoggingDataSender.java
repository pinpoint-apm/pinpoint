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

package com.navercorp.pinpoint.common.profiler.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;


/**
 * @author emeroad
 * @author netspider
 */
public class LoggingDataSender<REQ, RES> implements EnhancedDataSender<REQ, RES> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public boolean send(REQ data) {
        logger.info("send tBase:{}", data);
        return true;
    }


    @Override
    public void stop() {
        logger.info("LoggingDataSender stop");
    }

    @Override
    public boolean request(REQ data) {
        logger.info("request tBase:{}", data);
        return true;
    }

    @Override
    public boolean request(REQ data, int retry) {
        logger.info("request tBase:{} retry:{}", data, retry);
        return false;
    }


    @Override
    public boolean request(REQ data, BiConsumer<RES, Throwable> listener) {
        logger.info("request tBase:{} FutureListener:{}", data, listener);
        return false;
    }

}