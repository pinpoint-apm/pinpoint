package com.navercorp.pinpoint.web.frontend.export;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.config.UserConfigProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

@Component
public class ConfigPropertiesExporter implements FrontendConfigExporter {

    private final ConfigProperties webProperties;
    private final UserConfigProperties userConfigProperties;

    public ConfigPropertiesExporter(ConfigProperties webProperties, UserConfigProperties userConfigProperties) {
        this.webProperties = Objects.requireNonNull(webProperties, "webProperties");
        this.userConfigProperties = Objects.requireNonNull(userConfigProperties, "userConfigProperties");
    }

    @Override
    public void export(Map<String, Object> export) {
        export.put("sendUsage", webProperties.getSendUsage());
        export.put("editUserInfo", webProperties.getEditUserInfo());
        export.put("showActiveThread", webProperties.isShowActiveThread());
        export.put("showActiveThreadDump", webProperties.isShowActiveThreadDump());
        export.put("enableServerMapRealTime", webProperties.isEnableServerMapRealTime());
        export.put("showApplicationStat", webProperties.isShowApplicationStat());
        export.put("showStackTraceOnError", webProperties.isShowStackTraceOnError());
        export.put("showSystemMetric", webProperties.isShowSystemMetric());
        export.put("showUrlStat", webProperties.isShowUrlStat());
        export.put("openSource", userConfigProperties.isOpenSource());

        export.put("version", Version.VERSION);

        if (StringUtils.hasLength(webProperties.getSecurityGuideUrl())) {
            export.put("securityGuideUrl", webProperties.getSecurityGuideUrl());
        }
    }
}
