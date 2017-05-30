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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class LogStorageFactory implements StorageFactory {

    public final static Storage DEFAULT_STORAGE = new LogStorage();

    @Override
    public Storage createStorage(TraceRoot traceRoot) {
         // reuse because it has no states.
        return DEFAULT_STORAGE;
    }

    public static class LogStorage implements Storage {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        @Override
        public void store(SpanEvent spanEvent) {
            logger.debug("log spanEvent:{}", spanEvent);
        }

        @Override
        public void store(Span span) {
            logger.debug("log span:{}", span);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
