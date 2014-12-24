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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class EmptyAsyncQueueingExecutorListener<T> implements AsyncQueueingExecutorListener<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void execute(Collection<T> dtoList) {
        if (isDebug) {
            logger.debug("execute()");
        }
    }

    @Override
    public void execute(T dto) {
        if (isDebug) {
            logger.debug("execute()");
        }
    }
}
