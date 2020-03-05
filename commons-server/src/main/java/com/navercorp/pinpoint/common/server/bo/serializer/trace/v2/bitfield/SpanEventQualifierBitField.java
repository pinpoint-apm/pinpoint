/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield;

import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.profiler.encoding.BitFieldUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventQualifierBitField {

    public static final int SET_ASYNC = 0;

    @Deprecated
    public static byte buildBitField(SpanEventBo firstSpanEvent) {
        if (firstSpanEvent == null) {
            // no async bit field
            return 0;
        }

        byte bitField = 0;

        final int asyncId = firstSpanEvent.getAsyncId();
        final short asyncSequence = firstSpanEvent.getAsyncSequence();
        if (asyncId == -1 && asyncSequence == -1) {
            bitField = setAsync(bitField, false);
        } else {
            bitField = setAsync(bitField, true);
        }

        return bitField;
    }



    public static byte buildBitField(LocalAsyncIdBo localAsyncIdBo) {
        if (localAsyncIdBo == null) {
            // no async bit field
            return 0;
        }

        byte bitField = 0;

        final int asyncId = localAsyncIdBo.getAsyncId();
        final int asyncSequence = localAsyncIdBo.getSequence();
        if (asyncId == -1 && asyncSequence == -1) {
            bitField = setAsync(bitField, false);
        } else {
            bitField = setAsync(bitField, true);
        }

        return bitField;
    }

    private SpanEventQualifierBitField() {
    }


    public static boolean isSetAsync(byte bitField) {
        return BitFieldUtils.testBit(bitField, SET_ASYNC);
    }

    public static byte setAsync(byte bitField, boolean async) {
        return BitFieldUtils.setBit(bitField, SET_ASYNC, async);
    }


}
