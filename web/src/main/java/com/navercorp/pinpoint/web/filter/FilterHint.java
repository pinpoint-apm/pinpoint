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

import java.util.HashMap;
import java.util.List;

/**
 * Hint for fitering
 * 
 * @author netspider
 * 
 */
// FIXME don't know how to implement deserializer like this.
public class FilterHint extends HashMap<String, List<Object>> {

    private static final long serialVersionUID = -8765645836014210889L;

    public static final String EMPTY_JSON = "{}";

    public boolean containApplicationHint(String applicationName) {
        List<Object> list = get(applicationName);

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

        List<Object> list = get(applicationName);

        for (int i = 0; i < list.size(); i += 2) {
            if (endPoint.equals(list.get(i))) {
                if (serviceTypeCode == (Integer) list.get(i + 1)) {
                    return true;
                }
            }
        }

        return false;
    }
}
