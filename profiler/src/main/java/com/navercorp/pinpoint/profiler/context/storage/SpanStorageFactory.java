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

import com.navercorp.pinpoint.profiler.context.storage.flush.StorageFlusher;

/**
 * @author emeroad
 * @author Taejin Koo
 */
public class SpanStorageFactory implements StorageFactory {


    protected final StorageFlusher flusher;

    public SpanStorageFactory(StorageFlusher flusher) {
        if (flusher == null) {
            throw new NullPointerException("flusher must not be null");
        }

        this.flusher = flusher;
    }

    @Override
    public Storage createStorage() {
        return new SpanStorage(flusher);
    }

    @Override
    public String toString() {
        return "SpanStorageFactory{" +
                "flusher=" + flusher +
                '}';
    }

}
