/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.exceptiontrace.web;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * @author intr3p1d
 */
@PropertySources({
        @PropertySource(name = "ExceptionTracePropertySources", value = {ExceptionTraceWebPropertySources.EXCEPTION_TRACE, ExceptionTraceWebPropertySources.EXCEPTION_TRACE_ROOT}),
})
public class ExceptionTraceWebPropertySources {
    public static final String EXCEPTION_TRACE = "classpath:profiles/${pinpoint.profiles.active:release}/pinpoint-web-exceptiontrace.properties";
    public static final String EXCEPTION_TRACE_ROOT = "classpath:pinpoint-web-exceptiontrace-root.properties";
}
