package com.navercorp.pinpoint.alarm.web.frontend.export;

import com.navercorp.pinpoint.alarm.web.service.UserGroupMembershipValidator;
import com.navercorp.pinpoint.user.service.UserService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AlarmPropertiesExporterTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void exportMemberCheck(boolean enabled) {
        Map<String, Object> export = new HashMap<>();
        new AlarmPropertiesExporter(new UserGroupMembershipValidator(mock(UserService.class), mock(), enabled)).export(export);

        assertThat(export).containsEntry("alarmChannelMemberCheck", enabled);
    }
}
