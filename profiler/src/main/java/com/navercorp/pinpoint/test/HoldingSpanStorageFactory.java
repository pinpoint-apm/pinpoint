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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author hyungil.jeong
 */
public class HoldingSpanStorageFactory implements StorageFactory {

    private final PeekableDataSender<?> dataSender;

    public HoldingSpanStorageFactory(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (dataSender instanceof PeekableDataSender) {
            this.dataSender = (PeekableDataSender<?>)dataSender;
        } else {
            throw new IllegalArgumentException("dataSender must be an instance of PeekableDataSender.");
        }
    }

    @Override
    public Storage createStorage() {
        return new HoldingSpanStorage(this.dataSender);
    }

}
