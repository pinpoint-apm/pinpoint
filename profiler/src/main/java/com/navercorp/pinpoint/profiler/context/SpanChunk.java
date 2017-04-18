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

package com.navercorp.pinpoint.profiler.context;


import java.util.List;

import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author emeroad
 */
public class SpanChunk extends TSpanChunk {

    public SpanChunk(List<SpanEvent> spanEventList) {
        if (spanEventList == null) {
            throw new NullPointerException("spanEventList must not be null");
        }
        setSpanEventList((List) spanEventList);
    }

    @Override
    public void setServiceType(short serviceType) {
        super.setServiceType(serviceType);
    }

    @Override
    public void setServiceTypeIsSet(boolean value) {
        super.setServiceTypeIsSet(value);
    }

    @Override
    public short getServiceType() {
        return super.getServiceType();
    }
}
