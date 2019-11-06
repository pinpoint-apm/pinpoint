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
package com.navercorp.pinpoint.flink.vo;

import org.apache.thrift.TBase;

import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class RawData {

    private final TBase<?, ?> data;
    private final Map<String, String> metaInfo;

    public RawData(TBase<?, ?> data, Map<String,String> metaInfo) {
        this.data = Objects.requireNonNull(data, "data");
        this.metaInfo = Objects.requireNonNull(metaInfo, "metaInfo");
    }

    public TBase<?, ?> getData() {
        return data;
    }

    public String getMetaInfo(String key) {
        return metaInfo.get(key);
    }

}
