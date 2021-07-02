/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.agent.plugin.proxy.user;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeader;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeaderBuilder;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParser;

import java.util.Collections;
import java.util.List;

public class UserRequestParser implements ProxyRequestParser {
    private List<String> headerNameList = Collections.emptyList();

    @Override
    public String getHttpHeaderName() {
        return "";
    }

    @Override
    public List<String> getHttpHeaderNameList() {
        return this.headerNameList;
    }

    @Override
    public int getCode() {
        return UserRequestConstants.USER_REQUEST_TYPE.getCode();
    }

    @Override
    public void init(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            return;
        }
        this.headerNameList = profilerConfig.readList(UserRequestConstants.USER_PROXY_HEADER_NAME_LIST);
    }

    @Override
    @Deprecated
    public ProxyRequestHeader parse(String value) {
        return parseHeader("UNKNOWN", value);
    }

    @Override
    public ProxyRequestHeader parseHeader(String name, String value) {
        final ProxyRequestHeaderBuilder header = new ProxyRequestHeaderBuilder();
        header.setApp(name);
        for (String token : StringUtils.tokenizeToStringList(value, " ")) {
            if (token.startsWith("t=")) {
                final long receivedTimeMillis = toReceivedTimeMillis(token.substring(2));
                if (receivedTimeMillis > 0) {
                    header.setReceivedTimeMillis(receivedTimeMillis);
                    header.setValid(true);
                } else {
                    header.setValid(false);
                    header.setCause("invalid received time");
                    return header.build();
                }
            } else if (token.startsWith("D=")) {
                final long durationTimeMicroseconds = toDurationTimeMicros(token.substring(2));
                if (durationTimeMicroseconds > 0) {
                    header.setDurationTimeMicroseconds((int) durationTimeMicroseconds);
                }
            }
        }

        return header.build();
    }

    public long toReceivedTimeMillis(final String value) {
        if (value == null) {
            return 0;
        }

        // to milliseconds.
        return NumberUtils.parseLong(value, 0);
    }

    public int toDurationTimeMicros(final String value) {
        if (value == null) {
            return 0;
        }

        // to microseconds.
        return NumberUtils.parseInteger(value, 0);
    }
}