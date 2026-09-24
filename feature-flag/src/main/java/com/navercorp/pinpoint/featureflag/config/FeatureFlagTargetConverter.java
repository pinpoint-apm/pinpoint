package com.navercorp.pinpoint.featureflag.config;

import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.featureflag.service.properties.FeatureFlagTarget;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class FeatureFlagTargetConverter implements Converter<String, FeatureFlagTarget> {
    @Override
    public FeatureFlagTarget convert(@NonNull String applicationName) {
        return new FeatureFlagTarget(Service.DEFAULT.getServiceName(), applicationName);
    }
}
