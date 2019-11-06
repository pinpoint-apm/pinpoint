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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;

import org.slf4j.Logger;

/**
 * @author emeroad
 */
public class WriteFailFutureListener implements FutureListener {

    private final Logger logger;
    private final String message;
    private final String address;
    private final boolean isWarn;


    public WriteFailFutureListener(Logger logger, String message, String address) {
        this.logger = Assert.requireNonNull(logger, "logger");
        this.message = message;
        this.address = address;
        this.isWarn = logger.isWarnEnabled();
    }

    @Override
    public void onComplete(Future future) {
        if (!future.isSuccess()) {
            if (isWarn) {
                logger.warn("{} {}", message, address);
            }
        }
    }
}