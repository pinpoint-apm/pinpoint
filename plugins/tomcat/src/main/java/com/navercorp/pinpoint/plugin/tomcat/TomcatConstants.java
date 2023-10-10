/*
 * Copyright 2014 NAVER Corp.
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

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 *
 */
public final class TomcatConstants {
    private TomcatConstants() {
    }

    public static final ServiceType TOMCAT = ServiceTypeFactory.of(1010, "TOMCAT", RECORD_STATISTICS);
    public static final ServiceType TOMCAT_METHOD = ServiceTypeFactory.of(1011, "TOMCAT_METHOD");

    public static final String TOMCAT_SERVLET_ASYNC_SCOPE = "TomcatServletAsyncScope";
    public static final String TOMCAT_SERVLET_REQUEST_TRACE = "com.navercorp.pinpoint.trace";

    /**
     * The name of the request attribute that should be set by the container
     * when custom error-handling servlet or JSP page is invoked. The value of
     * the attribute is of type {@code java.lang.Throwable}. See the chapter
     * "Error Handling" in the Servlet Specification for details.
     *
     * @since Servlet 3.0
     */
    public static final String JAVAX_ERROR_EXCEPTION = "javax.servlet.error.exception";
    public static final String JAKARTA_ERROR_EXCEPTION = "jakarta.servlet.error.exception";
    public static final String[] TOMCAT_URI_USER_INPUT_ATTRIBUTE_KEYS = {"pinpoint.metric.uri-template"};
}