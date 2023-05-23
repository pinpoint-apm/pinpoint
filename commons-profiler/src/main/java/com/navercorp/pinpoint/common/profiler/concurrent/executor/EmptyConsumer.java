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

package com.navercorp.pinpoint.common.profiler.concurrent.executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

/**
 * @author emeroad
 */
public class EmptyConsumer<T> implements MultiConsumer<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void acceptN(Collection<T> dtoList) {
        logger.debug("execute(N)");
    }

    @Override
    public void accept(T dto) {
        logger.debug("execute()");
    }
}
