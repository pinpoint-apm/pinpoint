package com.navercorp.pinpoint.web.frontend.export;

import com.navercorp.pinpoint.web.frontend.config.ExperimentalProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class ExperimentalPropertiesExporter implements FrontendConfigExporter {

    private final ExperimentalProperties experimentalProperties;

    public ExperimentalPropertiesExporter(ExperimentalProperties experimentalProperties) {
        this.experimentalProperties = Objects.requireNonNull(experimentalProperties, "experimentalProperties");
    }

    @Override
    public void export(Map<String, Object> export) {
        export.putAll(experimentalProperties.getProperties());
    }
}
