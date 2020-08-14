/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.config;

/**
 * @author jaehong.kim
 */
public class ServerConfig {
    static final String TRACE_REQUEST_PARAM_PROPERTY_NAME = "profiler.server.tracerequestparam";
    static final String HIDE_PINPOINT_HEADER_PROPERTY_NAME = "profiler.server.hidepinpointheader";
    static final String EXCLUDE_URL_PROPERTY_NAME = "profiler.server.excludeurl";
    static final String REAL_IP_HEADER_PROPERTY_NAME = "profiler.server.realipheader";
    static final String REAL_IP_EMPTY_VALUE_PROPERTY_NAME = "profiler.server.realipemptyvalue";
    static final String EXCLUDE_METHOD_PROPERTY_NAME = "profiler.server.excludemethod";

    private ProfilerConfig config;

    public ServerConfig(final ProfilerConfig config) {
        if (config == null) {
            throw new NullPointerException("config");
        }
        this.config = config;
    }

    public boolean isHidePinpointHeader(final String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName");
        }

        final String propertyValue = config.readString(propertyName, "");
        if (!propertyValue.isEmpty()) {
            // Individual settings take precedence.
            return config.readBoolean(propertyName, true);
        }

        return config.readBoolean(HIDE_PINPOINT_HEADER_PROPERTY_NAME, true);
    }

    public boolean isTraceRequestParam(final String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName");
        }

        final String propertyValue = config.readString(propertyName, "");
        if (!propertyValue.isEmpty()) {
            // Individual settings take precedence.
            return config.readBoolean(propertyName, true);
        }

        return config.readBoolean(TRACE_REQUEST_PARAM_PROPERTY_NAME, true);
    }

    public Filter<String> getExcludeUrlFilter(final String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName");
        }

        final String excludeUrlPropertyValue = config.readString(propertyName, "");
        if (!excludeUrlPropertyValue.isEmpty()) {
            // Individual settings take precedence.
            return new ExcludePathFilter(excludeUrlPropertyValue);
        }
        final String serverExcludeUrlPropertyValue = config.readString(EXCLUDE_URL_PROPERTY_NAME, "");
        if (!serverExcludeUrlPropertyValue.isEmpty()) {
            return new ExcludePathFilter(serverExcludeUrlPropertyValue);
        }

        return new SkipFilter<String>();
    }

    public String getRealIpHeader(final String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName");
        }

        final String propertyValue = config.readString(propertyName, "");
        if (!propertyValue.isEmpty()) {
            // Individual settings take precedence.
            return propertyValue;
        }

        return config.readString(REAL_IP_HEADER_PROPERTY_NAME, "");
    }

    public String getRealIpEmptyValue(final String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName");
        }

        final String propertyValue = config.readString(propertyName, "");
        if (!propertyValue.isEmpty()) {
            // Individual settings take precedence.
            return propertyValue;
        }

        return config.readString(REAL_IP_EMPTY_VALUE_PROPERTY_NAME, "");
    }

    public Filter<String> getExcludeMethodFilter(final String propertyName) {
        if (propertyName == null) {
            throw new NullPointerException("propertyName");
        }

        final String propertyValue = config.readString(propertyName, "");
        if (!propertyValue.isEmpty()) {
            // Individual settings take precedence.
            return new ExcludeMethodFilter(propertyValue);
        }
        final String serverExcludeUrlPropertyValue = config.readString(EXCLUDE_METHOD_PROPERTY_NAME, "");
        if (!serverExcludeUrlPropertyValue.isEmpty()) {
            return new ExcludeMethodFilter(serverExcludeUrlPropertyValue);
        }

        return new SkipFilter<String>();
    }
}