/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * 存放请求/响应报文、url、头信息的span扩展块
 *
 * @author wangj881
 */
public class SpanWebInfo extends DefaultFrameAttachment {

    private final TraceRoot traceRoot;

    private final WebInfo webInfo;

    public SpanWebInfo(TraceRoot traceRoot, WebInfo webInfo) {
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot");
        this.webInfo = Assert.requireNonNull(webInfo, "webInfo");
    }

    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    public WebInfo getWebInfo() {
        return webInfo;
    }

    @Override
    public String toString() {
        return "SpanWebInfo{" +
                "traceRoot=" + traceRoot +
                ", webInfo=" + webInfo +
                '}';
    }
}
