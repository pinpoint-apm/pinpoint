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

package com.navercorp.pinpoint.profiler.monitor.codahale.gc;

import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

/**
 * Unknown Garbage collector
 * @author hyungil.jeong
 */
public class UnknownGarbageCollector implements GarbageCollector {

    public static final TJvmGcType GC_TYPE = TJvmGcType.UNKNOWN;

    @Override
    public int getTypeCode() {
        return GC_TYPE.getValue();
    }

    @Override
    public TJvmGc collect() {
        // return null to prevent data send.
        // (gc field of Thrift DTO is optional)
        return null;
    }

    @Override
    public String toString() {
        return "Unknown Garbage collector";
    }

}
