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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hint for fitering
 * 
 * @author netspider
 * 
 */
// FIXME don't know how to implement deserializer like this.
public class FilterHint {

    private static final long serialVersionUID = -8765645836014210889L;

    private Map<String, List<Object>> map;
    private final List<RpcHint> rpcHintList = new ArrayList<>();

    public static final String EMPTY_JSON = "{}";

    public FilterHint(Map<String, List<Object>> filterHintJsonObjectMap) {
        if (filterHintJsonObjectMap == null) {
            throw new NullPointerException("filterHintJsonObjectMap must not be null");
        }
        this.map = filterHintJsonObjectMap;
        for (Map.Entry<String, List<Object>> filterHintObject : filterHintJsonObjectMap.entrySet()) {
            List<RpcType> rpcTypeList = getRpcType(filterHintObject.getValue());
            rpcHintList.add(new RpcHint(filterHintObject.getKey(), rpcTypeList));
        }
    }

    private List<RpcType> getRpcType(List<Object> tpcTypeList) {
        final List<RpcType> returnRpcTypeLIst = new ArrayList<>();
        for (int i = 0; i < tpcTypeList.size(); i += 2) {
            final String urlHint = (String) tpcTypeList.get(i);
            final int urlServiceTypeCode = (int) tpcTypeList.get(i + 1);
            returnRpcTypeLIst.add(new RpcType(urlHint, urlServiceTypeCode));
        }
        return returnRpcTypeLIst;
    }

    public List<RpcHint> getRpcHintList(String sourceApplicationName) {
        if (sourceApplicationName == null) {
            throw new NullPointerException("sourceApplicationName must not be null");
        }
        final List<RpcHint> findRpcHintList = new ArrayList<>();
        for (RpcHint rpcHint : rpcHintList) {
            // TODO miss serviceType check
            if (rpcHint.getApplicationName().equals(sourceApplicationName)) {
                findRpcHintList.add(rpcHint);
            }
        }
        return findRpcHintList;
    }


    public boolean containApplicationHint(String applicationName) {
        List<Object> list = map.get(applicationName);

        if (list == null) {
            return false;
        } else {
            return !list.isEmpty();
        }
    }


    public boolean containApplicationEndpoint(String applicationName, String endPoint, int serviceTypeCode) {
        if (!containApplicationHint(applicationName)) {
            return false;
        }
        
        if (endPoint == null) {
            return false;
        }

        List<Object> list = map.get(applicationName);

        for (int i = 0; i < list.size(); i += 2) {
            final Object urlHint = list.get(i);
            if (endPoint.equals(urlHint)) {
                final Object urlServiceTypeCode = list.get(i + 1);
                if (serviceTypeCode == (Integer) urlServiceTypeCode) {
                    return true;
                }
            }
        }

        return false;
    }


}
