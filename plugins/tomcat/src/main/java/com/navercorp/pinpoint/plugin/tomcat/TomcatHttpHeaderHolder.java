/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.common.util.DelegateEnumeration;
import com.navercorp.pinpoint.common.util.EmptyEnumeration;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class TomcatHttpHeaderHolder {
    // tomcat only.
    private static final Map<String, String> NAME_SET = createMap();

    private static Map<String, String> createMap() {
        final Header[] headerList = Header.values();
        final Map<String, String> map = new HashMap<String, String>();
        for (Header header : headerList) {
            map.put(header.toString(), header.toString());
        }
        return map;
    }

    public static void setHeader(String name) {
        NAME_SET.put(name, name);
    }

    public static String getHeader(String name) {
        if (name == null) {
            return null;
        }
        if (!startWithPinpointHeader(name)) {
            return null;
        }
        return NAME_SET.get(name);
    }

    public static boolean hasHeader(String name) {
        return getHeader(name) != null;
    }

    public static Enumeration getHeaders(String name) {
        if (name == null) {
            return null;
        }
        final String header = getHeader(name);
        if (header == null) {
            return null;
        }
        // if pinpoint header
        return new EmptyEnumeration();
    }

    public static Enumeration filteredHeaderNames(final Enumeration enumeration) {
        return new DelegateEnumeration(enumeration, FILTER);
    }

    private static DelegateEnumeration.Filter FILTER = new DelegateEnumeration.Filter() {
        @Override
        public boolean filter(Object o) {
            if (o instanceof String) {
                return hasHeader((String) o);
            }
            return false;
        }
    };

    private static boolean startWithPinpointHeader(String name) {
        return name.startsWith("Pinpoint-");
    }
}
