package com.navercorp.pinpoint.web.frontend.export;

import com.navercorp.pinpoint.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceConfigExporterTest {

    @Test
    void exportLoginIdWithoutUserTableRow() {
        UserService userService = mock(UserService.class);
        when(userService.getUserIdFromSecurity()).thenReturn("alice");

        Map<String, Object> export = new HashMap<>();
        new UserServiceConfigExporter(userService).export(export);

        assertThat(export).containsEntry("userId", "alice").doesNotContainKey("userName");
    }

    @Test
    void exportNothingWithoutLogin() {
        UserService userService = mock(UserService.class);
        when(userService.getUserIdFromSecurity()).thenReturn("");

        Map<String, Object> export = new HashMap<>();
        new UserServiceConfigExporter(userService).export(export);

        assertThat(export).isEmpty();
    }
}
