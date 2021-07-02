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

import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestType;
import com.navercorp.pinpoint.common.util.StringUtils;

public class UserRequestType implements ProxyRequestType {
    private static final String DEFAULT_DISPLAY_NAME = "PROXY(USER)";

    @Override
    public String getHttpHeaderName() {
        return "Pinpoint-ProxyUser";
    }

    @Override
    @Deprecated
    public String getDisplayName() {
        return getDisplayName(DEFAULT_DISPLAY_NAME);
    }

    @Override
    public String getDisplayName(String name) {
        if (StringUtils.isEmpty(name)) {
            return DEFAULT_DISPLAY_NAME;
        }
        return name;
    }

    @Override
    public int getCode() {
        return 4;
    }

    @Override
    public boolean useApp() {
        return false;
    }
}