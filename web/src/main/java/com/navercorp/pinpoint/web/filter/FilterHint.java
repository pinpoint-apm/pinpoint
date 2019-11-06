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
import com.navercorp.pinpoint.web.filter.deserializer.FilterHintListJsonDeserializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hint for filtering
 *
 * @author netspider
 *
 */
@JsonDeserialize(using = FilterHintListJsonDeserializer.class)
public class FilterHint {

    public static final String EMPTY_JSON = "{}";

    private final List<RpcHint> rpcHintList;


    public FilterHint(List<RpcHint> rpcHintList) {
        if (rpcHintList == null) {
            rpcHintList = Collections.emptyList();
        }
        this.rpcHintList = rpcHintList;
    }


    public List<RpcHint> getRpcHintList(String targetApplicationName) {
        if (targetApplicationName == null) {
            throw new NullPointerException("targetApplicationName");
        }
        final List<RpcHint> findRpcHintList = new ArrayList<>();
        for (RpcHint rpcHint : rpcHintList) {
            // TODO miss serviceType
            if (rpcHint.getApplicationName().equals(targetApplicationName)) {
                findRpcHintList.add(rpcHint);
            }
        }
        return findRpcHintList;
    }


    public boolean containApplicationHint(String applicationName) {
        for (RpcHint rpcHint : rpcHintList) {
            if(rpcHint.getApplicationName().equals(applicationName)) {
                return true;
            }
        }
        return false;
    }


    public boolean containApplicationEndpoint(String applicationName, String endPoint, int serviceTypeCode) {
        if (!containApplicationHint(applicationName)) {
            return false;
        }

        if (endPoint == null) {
            return false;
        }

        for (RpcHint rpcHint : rpcHintList) {
            if (rpcHint.getApplicationName().equals(applicationName)) {
                for (RpcType rpcType : rpcHint.getRpcTypeList()) {
                    if (rpcType.isMatched(endPoint, serviceTypeCode)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }


    public int size() {
        return rpcHintList.size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FilterHint{");
        sb.append("rpcHintList=").append(rpcHintList);
        sb.append('}');
        return sb.toString();
    }
}
