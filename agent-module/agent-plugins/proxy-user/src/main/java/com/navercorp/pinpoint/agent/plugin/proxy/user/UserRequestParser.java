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
    static final String PREFIX_RECEIVED = "t=";
    static final String PREFIX_DURATION = "D=";

    private List<String> headerNameList = Collections.emptyList();

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
        headerNameList = profilerConfig.readList(UserRequestConstants.USER_PROXY_HEADER_NAME_LIST);
    }

    @Override
    public ProxyRequestHeader parseHeader(String name, String value) {
        final ProxyRequestHeaderBuilder header = new ProxyRequestHeaderBuilder();
        header.setApp(name);

        final List<String> tokenList = StringUtils.tokenizeToStringList(value, " ");
        final String receivedTimeValue = findValue(tokenList, PREFIX_RECEIVED);
        if (receivedTimeValue != null) {
            final long receivedTimeMillis = toReceivedTimeMillis(receivedTimeValue);
            if (receivedTimeMillis > 0) {
                header.setReceivedTimeMillis(receivedTimeMillis);
                header.setValid(true);
            } else {
                header.setValid(false);
                header.setCause("invalid received time");
                return header.build();
            }
        } else {
            header.setValid(false);
            header.setCause("not found received time");
            return header.build();
        }
        final String durationTimeValue = findValue(tokenList, PREFIX_DURATION);
        if (durationTimeValue != null) {
            final long durationTimeMicroseconds = toDurationTimeMicros(durationTimeValue);
            if (durationTimeMicroseconds > 0) {
                header.setDurationTimeMicroseconds((int) durationTimeMicroseconds);
            }
        }

        return header.build();
    }

    String findValue(List<String> tokenList, String prefix) {
        for (String token : tokenList) {
            if (token.startsWith(prefix)) {
                return token.substring(prefix.length());
            }
        }
        return null;
    }

    long toReceivedTimeMillis(final String value) {
        if (value == null) {
            return 0;
        }

        final int length = value.length();
        if (length < 13) {
            return 0;
        }

        if (length >= 16) {
            // apache - microseconds
            return NumberUtils.parseLong(value.substring(0, length - 3), 0);
        }
        final int millisPosition = value.lastIndexOf('.');
        if (millisPosition != -1) {
            // nginx - seconds.milliseconds
            // e.g. 1504230492.763
            if (millisPosition < 10 || length - millisPosition != 4) {
                // invalid format.
                return 0;
            }
            final String strValue = value.substring(0, millisPosition) + value.substring(millisPosition + 1);
            return NumberUtils.parseLong(strValue, 0);
        }
        // app - milliseconds
        return NumberUtils.parseLong(value, 0);
    }

    int toDurationTimeMicros(final String value) {
        if (value == null) {
            return 0;
        }

        final int length = value.length();
        final int millisPosition = value.lastIndexOf('.');
        if (millisPosition != -1) {
            // nginx
            // e.g. 0.000
            if (length - millisPosition != 4) {
                // invalid format.
                return 0;
            }
            final String strValue = value.substring(0, millisPosition) + value.substring(millisPosition + 1);
            return NumberUtils.parseInteger(strValue, 0) * 1000;
        } else {
            // apache, app
            // to microseconds.
            return NumberUtils.parseInteger(value, 0);
        }
    }
}