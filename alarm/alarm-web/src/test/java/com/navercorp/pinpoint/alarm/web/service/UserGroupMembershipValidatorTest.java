package com.navercorp.pinpoint.alarm.web.service;

import com.navercorp.pinpoint.user.service.UserGroupService;
import com.navercorp.pinpoint.user.service.UserService;
import org.springframework.beans.factory.ObjectProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserGroupMembershipValidatorTest {

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void memberPasses() {
        assertThatCode(() -> validator("alice", "team-a").validate("team-a")).doesNotThrowAnyException();
    }

    @Test
    void nonMemberIsRejected() {
        assertThatThrownBy(() -> validator("alice", "team-a").validate("team-b"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not a member");
    }

    @Test
    void noLoginIsRejected() {
        assertThatThrownBy(() -> validator("").validate("team-a"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No login user");
    }

    @Test
    void adminPassesAnyGroup() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("eve", null, "ROLE_ADMIN"));
        UserGroupMembershipValidator validator = validator("eve");

        assertThatCode(() -> validator.validate("team-b")).doesNotThrowAnyException();
    }

    @Test
    void userWithoutAdminRoleIsChecked() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("carol", null, "ROLE_USER"));

        assertThatThrownBy(() -> validator("carol").validate("team-b")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void checkOffPassesAnyGroup() {
        UserGroupMembershipValidator validator =
                new UserGroupMembershipValidator(mock(UserService.class), mock(), false);

        assertThatCode(() -> validator.validate("team-b")).doesNotThrowAnyException();
    }

    private static UserGroupMembershipValidator validator(String userId, String... memberOf) {
        UserService userService = mock(UserService.class);
        when(userService.getUserIdFromSecurity()).thenReturn(userId);
        UserGroupService userGroupService = mock(UserGroupService.class);
        for (String groupId : memberOf) {
            when(userGroupService.checkValid(userId, groupId)).thenReturn(true);
        }
        ObjectProvider<UserGroupService> userGroupProvider = mock();
        when(userGroupProvider.getObject()).thenReturn(userGroupService);
        return new UserGroupMembershipValidator(userService, userGroupProvider, true);
    }
}
