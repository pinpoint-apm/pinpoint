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

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * @author HyunGil Jeong
 * @author Jongjin.Bae
 */
public class ConfigProperties {

    private final Logger logger = LogManager.getLogger(ConfigProperties.class);

    @Value("${config.sendUsage:true}")
    private boolean sendUsage;
    
    @Value("${config.editUserInfo:true}")
    private boolean editUserInfo;

    @Value("${config.show.activeThread:false}")
    private boolean showActiveThread;

    @Value("${config.show.activeThreadDump:false}")
    private boolean showActiveThreadDump;

    @Value("${config.enable.activeThreadDump:false}")
    private boolean enableActiveThreadDump;

    @Value("${config.enable.serverMapRealTime:false}")
    private boolean enableServerMapRealTime;

    @Value("${config.openSource:true}")
    private boolean openSource;

    @Value("${security.guide.url:#{null}}")
    private String securityGuideUrl;

    @Value("${config.show.applicationStat:false}")
    private boolean showApplicationStat;

    @Value("${config.show.stackTraceOnError:true}")
    private boolean showStackTraceOnError;

    @Value("${config.show.systemMetric:false}")
    private boolean showSystemMetric;

    @Value("${config.show.urlStat:false}")
    private boolean showUrlStat;

    @Value("${config.show.exceptionTrace:false}")
    private boolean showExceptionTrace;

    @Value("${config.show.sqlStat:false}")
    private boolean showSqlStat;

    @Value("${pinpoint.modules.web.otlpmetric.enabled:false}")
    private boolean showOtlpMetric;

    @Value("${pinpoint.modules.web.inspector.enabled:false}")
    private boolean showInspector;

    @Value("${pinpoint.modules.web.heatmap.enabled:false}")
    private boolean showHeatmap;

    @Value("${websocket.allowedOrigins:#{null}}")
    private String webSocketAllowedOrigins;

    @Value("${pinpoint.modules.web.webhook:false}")
    private boolean webhookEnable;

    @Value("${web.servermap.api.period.max:2}")
    private int serverMapPeriodMax;

    @Value("${web.servermap.api.period.interval:5m,20m,1h,3h,6h,12h,1d,2d}")
    private List<String> serverMapPeriodInteval;

    @Value("${web.inspector.api.period.max:42}")
    private int inspectorPeriodMax;

    @Value("${web.inspector.api.period.interval:5m,20m,1h,3h,6h,12h,1d,2d,1w,3w,6w}")
    private List<String> inspectorPeriodInteval;

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

    public boolean isShowSystemMetric() {
        return showSystemMetric;
    }

    public boolean isShowOtlpMetric() {
        return showOtlpMetric;
    }

    public boolean isShowUrlStat() {
        return showUrlStat;
    }

    public boolean isShowExceptionTrace() {
        return showExceptionTrace;
    }

    public boolean isShowSqlStat() {
        return showSqlStat;
    }

    public String getWebSocketAllowedOrigins() {
        return webSocketAllowedOrigins;
    }

    public boolean isWebhookEnable() {
        return webhookEnable;
    }

    public int getServerMapPeriodMax() {
        return serverMapPeriodMax;
    }

    public List<String> getServerMapPeriodInteval() {
        return serverMapPeriodInteval;
    }

    public int getInspectorPeriodMax() {
        return inspectorPeriodMax;
    }

    public List<String> getInspectorPeriodInteval() {
        return inspectorPeriodInteval;
    }

    public boolean isShowInspector() {
        return showInspector;
    }

    public boolean isShowHeatmap() {
        return showHeatmap;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    @Override
    public String toString() {
        return "ConfigProperties{" +
                "sendUsage=" + sendUsage +
                ", editUserInfo=" + editUserInfo +
                ", showActiveThread=" + showActiveThread +
                ", showActiveThreadDump=" + showActiveThreadDump +
                ", enableActiveThreadDump=" + enableActiveThreadDump +
                ", enableServerMapRealTime=" + enableServerMapRealTime +
                ", openSource=" + openSource +
                ", securityGuideUrl='" + securityGuideUrl + '\'' +
                ", showApplicationStat=" + showApplicationStat +
                ", showStackTraceOnError=" + showStackTraceOnError +
                ", webSocketAllowedOrigins=" + webSocketAllowedOrigins +
                ", showOtlpMetric=" + showOtlpMetric +
                ", serverMapPeriodMax=" + serverMapPeriodMax +
                ", serverMapPeriodInterval=" + serverMapPeriodInteval +
                ", inspectorPeriodMax=" + inspectorPeriodMax +
                ", inspectorPeriodInterval=" + inspectorPeriodInteval +
                '}';
    }
}
