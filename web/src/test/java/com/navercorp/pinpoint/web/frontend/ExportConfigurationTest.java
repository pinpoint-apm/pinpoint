package com.navercorp.pinpoint.web.frontend;

import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.frontend.export.ConfigPropertiesExporter;
import com.navercorp.pinpoint.web.frontend.export.ExperimentalPropertiesExporter;
import com.navercorp.pinpoint.web.frontend.export.FrontendConfigExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        ConfigProperties.class,

        ExportConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class ExportConfigurationTest {

    @Autowired
    List<FrontendConfigExporter> exporters;

    @Test
    void lookup() {
        assertThat(exporters)
                .hasSize(2)
                .map(exporter -> (Class) exporter.getClass())
                .contains(ConfigPropertiesExporter.class, ExperimentalPropertiesExporter.class);
    }
}

