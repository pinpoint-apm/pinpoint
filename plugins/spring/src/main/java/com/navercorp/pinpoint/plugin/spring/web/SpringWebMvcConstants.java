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

package com.navercorp.pinpoint.plugin.spring.web;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import java.util.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SpringWebMvcConstants {
    private SpringWebMvcConstants() {
    }

    public static final ServiceType SPRING_MVC = ServiceTypeFactory.of(5051, "SPRING_MVC", "SPRING");

    public static final ServiceType BODY_OBTAIN_SERVICE_TYPE = ServiceTypeFactory.of(5088, "SPRING_OBTAIN", "SPRING_OBTAIN");

    public static final String BODY_OBTAIN_SCOPE = "BodyObtainScope";

    public static final String PRE_HANDLE_SCOPE = "PreHandleScope";

    public static final List<String> TRACE_METHODS = Collections.unmodifiableList(unmodifiableList());

    private static List<String> unmodifiableList() {
        List<String> list = new ArrayList<String>(4);
        list.add("GET");
        list.add("POST");
        list.add("PUT");
        list.add("DELETE");
        return list;
    }
}
