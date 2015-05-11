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

import com.navercorp.pinpoint.profiler.util.ObjectPool;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;

/**
 * @author Taejin Koo
 */
public class SpanStreamSendDataFactory {

    public static final int DEFAULT_UDP_MAX_BUFFER_SIZE = 65507;
    // MAX - start - end
    public static final int DEFAULT_UDP_MAX_GATHERING_COMPONENTS_COUNT = 16;

    
    private final int maxPacketSize;
    private final int maxGatheringComponentCount;
    private final ObjectPool<HeaderTBaseSerializer> serializerPool;

    
    public SpanStreamSendDataFactory(ObjectPool<HeaderTBaseSerializer> serializerPool) {
        this(DEFAULT_UDP_MAX_BUFFER_SIZE, DEFAULT_UDP_MAX_GATHERING_COMPONENTS_COUNT, serializerPool);
    }
    
    public SpanStreamSendDataFactory(int maxPacketSize, int maxGatheringComponentCount, ObjectPool<HeaderTBaseSerializer> serializerPool) { this.maxPacketSize = maxPacketSize;
        this.maxGatheringComponentCount = maxGatheringComponentCount;
        this.serializerPool = serializerPool;
    }

    public SpanStreamSendData create() {
        return new SpanStreamSendData(maxPacketSize, maxGatheringComponentCount, serializerPool);
    }

}
