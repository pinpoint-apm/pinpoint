/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.collector.sender;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.request.FlinkRequest;
import com.navercorp.pinpoint.thrift.io.FlinkTBaseLocator;
import org.apache.thrift.TBase;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class FlinkRequestFactory {

    private final FlinkTBaseLocator locator;

    public FlinkRequestFactory(FlinkTBaseLocator flinkTBaseLocator) {
        locator = Assert.requireNonNull(flinkTBaseLocator, "flinkTBaseLocator must not be null");
    }

    public FlinkRequest createFlinkRequest(TBase<?,?> data) {
        Header header = locator.createHeaderByMeta(data);
        return new FlinkRequest(header, data);
    }
}
