package com.navercorp.pinpoint.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.core.service.CoreAlarmDataSourceProvider;
import com.navercorp.pinpoint.alarm.core.vo.CoreAlarmDataSource;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.validation.ConditionValidator;
import com.navercorp.pinpoint.alarm.validation.FilterKeyValidator;
import com.navercorp.pinpoint.alarm.web.AlarmTemplatePreset;
import com.navercorp.pinpoint.alarm.web.AlarmTemplatePresetLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoreAlarmPresetsTest {

    private final AlarmDataSourceRegistry registry = new AlarmDataSourceRegistry(
            List.of(new CoreAlarmDataSourceProvider()));

    // The registry indexes rules by the code alarm_rule_v2.data_source stores, so a core data
    // source it cannot resolve rejects every rule on it.
    @Test
    void theCoreDataSourcesResolveThroughTheRegistry() {
        for (CoreAlarmDataSource dataSource : CoreAlarmDataSource.values()) {
            assertThat(registry.get(dataSource.name())).isEqualTo(dataSource);
        }
    }

    // The core presets ship in a module whose own tests run no validator over them.
    @Test
    void theCorePresetsLoad() {
        AlarmTemplatePresetLoader loader = new AlarmTemplatePresetLoader(new ObjectMapper(),
                new ConditionValidator(), new FilterKeyValidator(), registry);

        assertThat(loader.getPresets()).extracting(AlarmTemplatePreset::name)
                .extracting(AlarmTemplatePreset.LocalizedText::en)
                .containsExactlyInAnyOrder("Agent Health Basics", "Response Quality Basics");
    }
}
