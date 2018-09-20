/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class ConfigProperties {

    @Value("#{pinpointWebProps['config.sendUsage'] ?: true}")
    private boolean sendUsage;
    
    @Value("#{pinpointWebProps['config.editUserInfo'] ?: true}")
    private boolean editUserInfo;

    @Value("#{pinpointWebProps['config.show.activeThread'] ?: false}")
    private boolean showActiveThread;

    @Value("#{pinpointWebProps['config.show.activeThreadDump'] ?: false}")
    private boolean showActiveThreadDump;

    @Value("#{pinpointWebProps['config.enable.activeThreadDump'] ?: false}")
    private boolean enableActiveThreadDump;

    @Value("#{pinpointWebProps['config.enable.serverMapRealTime'] ?: false}")
    private boolean enableServerMapRealTime;

    @Value("#{pinpointWebProps['config.openSource'] ?: true}")
    private boolean openSource;

    @Value("#{pinpointWebProps['security.guide.url']}")
    private String securityGuideUrl;

    @Value("#{pinpointWebProps['config.show.applicationStat'] ?: false}")
    private boolean showApplicationStat;

    @Value("#{pinpointWebProps['config.show.stackTraceOnError'] ?: true}")
    private boolean showStackTraceOnError;

    @Value("#{pinpointWebProps['websocket.allowedOrigins']}")
    private String webSocketAllowedOrigins;

    public String getSecurityGuideUrl() {
        return securityGuideUrl;
    }

    public boolean getEditUserInfo() {
        return editUserInfo;
    }

    public boolean getSendUsage() {
        return this.sendUsage;
    }

    public boolean isShowActiveThread() {
        return this.showActiveThread;
    }

    public boolean isShowActiveThreadDump() {
        return showActiveThreadDump;
    }

    public boolean isEnableActiveThreadDump() {
        return enableActiveThreadDump;
    }

    public boolean isEnableServerMapRealTime() {
        return enableServerMapRealTime;
    }

    public boolean isOpenSource() {
        return this.openSource;
    }

    public boolean isShowApplicationStat() {
        return this.showApplicationStat;
    }

    public boolean isShowStackTraceOnError() {
        return showStackTraceOnError;
    }

    public String getWebSocketAllowedOrigins() {
        return webSocketAllowedOrigins;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigProperties{");
        sb.append("sendUsage=").append(sendUsage);
        sb.append(", editUserInfo=").append(editUserInfo);
        sb.append(", showActiveThread=").append(showActiveThread);
        sb.append(", showActiveThreadDump=").append(showActiveThreadDump);
        sb.append(", enableActiveThreadDump=").append(enableActiveThreadDump);
        sb.append(", enableServerMapRealTime=").append(enableServerMapRealTime);
        sb.append(", openSource=").append(openSource);
        sb.append(", securityGuideUrl='").append(securityGuideUrl).append('\'');
        sb.append(", showApplicationStat=").append(showApplicationStat);
        sb.append(", showStackTraceOnError=").append(showStackTraceOnError);
        sb.append(", webSocketAllowedOrigins=").append(webSocketAllowedOrigins);
        sb.append('}');
        return sb.toString();
    }

}
