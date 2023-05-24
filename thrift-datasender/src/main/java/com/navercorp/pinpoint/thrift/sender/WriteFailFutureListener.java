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
package com.navercorp.pinpoint.thrift.sender;

import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author emeroad
 */
public class WriteFailFutureListener<T> implements BiConsumer<T, Throwable> {

    private final Logger logger;
    private final String message;
    private final String address;

    public WriteFailFutureListener(Logger logger, String message, String address) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.message = message;
        this.address = address;
    }

    @Override
    public void accept(T t, Throwable throwable) {
        if (throwable != null) {
            logger.warn("{} {}", message, address);
        }
    }
}