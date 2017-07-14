/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentStatDataPointCodec {

    public void encodeTimestamps(Buffer buffer, List<Long> timestamps) {
        long prevTimestamp = timestamps.get(0);
        long prevDelta = 0;
        // skip first timestamp as this value is encoded as the qualifier and delta is meaningless
        for (int i = 1; i < timestamps.size(); ++i) {
            long timestamp = timestamps.get(i);
            long timestampDelta = timestamp - prevTimestamp;
            buffer.putVLong(timestampDelta - prevDelta);
            prevTimestamp = timestamp;
            prevDelta = timestampDelta;
        }
    }

    public List<Long> decodeTimestamps(long initialTimestamp, Buffer buffer, int numValues) {
        List<Long> timestamps = new ArrayList<Long>(numValues);
        timestamps.add(initialTimestamp);
        long prevTimestamp = initialTimestamp;
        long prevDelta = 0;
        // loop through numValues - 1 as the first timestamp is gotten from the qualifier
        for (int i = 0; i < numValues - 1; ++i) {
            long timestampDelta = prevDelta + buffer.readVLong();
            long timestamp = prevTimestamp + timestampDelta;
            timestamps.add(timestamp);
            prevTimestamp = timestamp;
            prevDelta = timestampDelta;
        }
        return timestamps;
    }

    public <T> void encodeValues(Buffer buffer, EncodingStrategy<T> encodingStrategy, List<T> values) {
        encodingStrategy.encodeValues(buffer, values);
    }

    public <T> List<T> decodeValues(Buffer buffer, EncodingStrategy<T> encodingStrategy, int numValues) {
        return encodingStrategy.decodeValues(buffer, numValues);
    }
}


