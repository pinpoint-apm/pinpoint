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

package com.navercorp.pinpoint.agent.plugin.proxy.app;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeader;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeaderBuilder;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParser;

/**
 * @author jaehong.kim
 */
public class AppRequestParser implements ProxyRequestParser {

    @Override
    public String getHttpHeaderName() {
        return AppRequestConstants.APP_REQUEST_TYPE.getHttpHeaderName();
    }

    @Override
    public int getCode() {
        return AppRequestConstants.APP_REQUEST_TYPE.getCode();
    }


    @Override
    public ProxyRequestHeader parse(String value) {
        final ProxyRequestHeaderBuilder header = new ProxyRequestHeaderBuilder();
        for (String token : StringUtils.tokenizeToStringList(value, " ")) {
            if (token.startsWith("t=")) {
                // convert to milliseconds from microseconds.
                final long receivedTimeMillis = toReceivedTimeMillis(token.substring(2));
                if (receivedTimeMillis > 0) {
                    header.setReceivedTimeMillis(receivedTimeMillis);
                    header.setValid(true);
                } else {
                    // stop.
                    header.setValid(false);
                    header.setCause("invalid received time");
                    return header.build();
                }
            } else if (token.startsWith("app=")) {
                final String app = token.substring(4).trim();
                if (!app.isEmpty()) {
                    header.setApp(app);
                }
            }
        }
        return header.build();
    }

    private long toReceivedTimeMillis(final String value) {
        if (value == null) {
            return 0;
        }

        // to milliseconds.
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }
}