/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.recorder.proxy;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author jaehong.kim
 */
public class ProxyRequestAnnotationFactory {
    public static final int APP_MAX_LENGTH = 32;

    public AnnotationKey getAnnotationKey() {
        return AnnotationKey.PROXY_HTTP_HEADER;
    }

    public Object getAnnotationValue(final int code, final ProxyRequestHeader header) {
        if (header.getApp() != null) {
            return new LongIntIntByteByteStringValue(header.getReceivedTimeMillis(), code, header.getDurationTimeMicroseconds(), header.getIdlePercent(), header.getBusyPercent(), StringUtils.abbreviate(header.getApp(), APP_MAX_LENGTH));
        }
        return new LongIntIntByteByteStringValue(header.getReceivedTimeMillis(), code, header.getDurationTimeMicroseconds(), header.getIdlePercent(), header.getBusyPercent(), null);
    }

}
