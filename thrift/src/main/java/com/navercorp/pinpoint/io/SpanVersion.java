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

package com.navercorp.pinpoint.io;

import com.navercorp.pinpoint.thrift.dto.TraceConstants;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanVersion {
    public static final byte TRACE_V1 = TraceConstants.TRACE_V1;

    public static final byte TRACE_V2 = TraceConstants.TRACE_V2;

    public static boolean supportedVersionRange(byte version) {
        if (version >= TRACE_V1 && version <= TRACE_V2) {
            return true;
        }
        return false;
    }
}
