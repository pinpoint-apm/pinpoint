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

package com.navercorp.pinpoint.web.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navercorp.pinpoint.web.filter.deserializer.RpcHintJsonDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
@JsonDeserialize(using = RpcHintJsonDeserializer.class)
public class RpcHint {

    private final String applicationName;
    // TODO fix serviceType miss
//        private ServiceType serviceType;
    private final List<RpcType> rpcTypeList;

    public RpcHint(String applicationName, List<RpcType> rpcTypeList) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");

        Objects.requireNonNull(rpcTypeList, "rpcTypeList");
        this.rpcTypeList = Collections.unmodifiableList(rpcTypeList);
    }


    public String getApplicationName() {
        return applicationName;
    }

    public List<RpcType> getRpcTypeList() {
        return rpcTypeList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RpcHint{");
        sb.append("applicationName='").append(applicationName).append('\'');
        sb.append(", rpcTypeList=").append(rpcTypeList);
        sb.append('}');
        return sb.toString();
    }
}
