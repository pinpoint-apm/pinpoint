/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.thrift;

import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public abstract class AbstractClientFactoryProvider {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected int getByteSize(String value, int defaultSize) {
        try {
            long byteSize = ByteSizeUnit.getByteSize(value);
            if (byteSize > Integer.MAX_VALUE) {
                logger.warn("must be writeBufferWaterMark({}) range is 0 ~ {}", value, Integer.MAX_VALUE);
                byteSize = Integer.MAX_VALUE;
            } else if (byteSize <= 0) {
                logger.warn("must be writeBufferWaterMark({}) range is 0 ~ {}", value, Integer.MAX_VALUE);
                byteSize = defaultSize;
            }
            return (int) byteSize;
        } catch (Exception e) {
            logger.warn("Failed to get byteSize({}). byteSize will be defaultSize:{}", value, defaultSize);
        }
        return defaultSize;
    }

}
