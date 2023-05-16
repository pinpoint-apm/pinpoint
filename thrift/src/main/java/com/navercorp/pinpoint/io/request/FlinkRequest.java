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
package com.navercorp.pinpoint.io.request;

import java.util.Objects;
import com.navercorp.pinpoint.io.header.HeaderEntity;
import org.apache.thrift.TBase;

/**
 * @author minwoo.jung
 */
public class FlinkRequest {

    private final HeaderEntity headerEntity;
    private final TBase<?, ?> data;

    public FlinkRequest(HeaderEntity headerEntity, TBase<?, ?> data) {
        this.headerEntity = Objects.requireNonNull(headerEntity, "headerEntity");
        this.data = Objects.requireNonNull(data, "data");
    }

    public HeaderEntity getHeaderEntity() {
        return headerEntity;
    }

    public TBase<?, ?> getData() {
        return data;
    }
}
