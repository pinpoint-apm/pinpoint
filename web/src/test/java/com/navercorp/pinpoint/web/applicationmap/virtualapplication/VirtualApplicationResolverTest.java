/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.virtualapplication;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.virtualapplication.VirtualApplicationProperties.ApplicationRef;
import com.navercorp.pinpoint.web.applicationmap.virtualapplication.VirtualApplicationProperties.VirtualApplicationRule;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VirtualApplicationResolverTest {

    @Test
    void resolve_match_expandsToConfiguredMembers() {
        Application memberA = new Application("CAFE.A", ServiceType.STAND_ALONE);
        Application memberB = new Application("CAFE.B", ServiceType.STAND_ALONE);

        ApplicationValidator validator = mock(ApplicationValidator.class);
        when(validator.newApplication("CAFE.A", "SPRING_BOOT")).thenReturn(memberA);
        when(validator.newApplication("CAFE.B", "TOMCAT")).thenReturn(memberB);

        VirtualApplicationRule rule = newRule("CAFESERVICE",
                ref("CAFE.A", "SPRING_BOOT"),
                ref("CAFE.B", "TOMCAT"));

        VirtualApplicationResolver resolver = VirtualApplicationResolver.of(List.of(rule), validator);

        Application input = new Application("CAFESERVICE", serviceTypeNamed("SERVICE"));
        List<Application> result = resolver.resolve(input);

        assertThat(result).containsExactly(memberA, memberB);
    }

    @Test
    void resolve_noMatch_returnsSingletonOfInput() {
        ApplicationValidator validator = mock(ApplicationValidator.class);
        when(validator.newApplication("CAFE.A", "SPRING_BOOT"))
                .thenReturn(new Application("CAFE.A", ServiceType.STAND_ALONE));

        VirtualApplicationResolver resolver = VirtualApplicationResolver.of(
                List.of(newRule("CAFESERVICE", ref("CAFE.A", "SPRING_BOOT"))),
                validator);

        Application input = new Application("UNKNOWN_APP", serviceTypeNamed("SERVICE"));
        List<Application> result = resolver.resolve(input);

        assertThat(result).containsExactly(input);
    }

    @Test
    void resolve_nonServiceType_returnsSingletonOfInput() {
        ApplicationValidator validator = mock(ApplicationValidator.class);
        when(validator.newApplication("CAFE.A", "SPRING_BOOT"))
                .thenReturn(new Application("CAFE.A", ServiceType.STAND_ALONE));

        VirtualApplicationResolver resolver = VirtualApplicationResolver.of(
                List.of(newRule("CAFESERVICE", ref("CAFE.A", "SPRING_BOOT"))),
                validator);

        Application input = new Application("CAFESERVICE", serviceTypeNamed("SPRING_BOOT"));
        assertThat(resolver.resolve(input)).containsExactly(input);
    }

    @Test
    void emptyResolver_alwaysReturnsSingletonOfInput() {
        VirtualApplicationResolver resolver = VirtualApplicationResolver.emptyResolver();

        Application input = new Application("ANY", ServiceType.STAND_ALONE);
        assertThat(resolver.resolve(input)).containsExactly(input);
    }

    @Test
    void of_validatesMembers_atBootTime() {
        ApplicationValidator validator = mock(ApplicationValidator.class);
        Application memberA = new Application("CAFE.A", ServiceType.STAND_ALONE);
        when(validator.newApplication("CAFE.A", "SPRING_BOOT")).thenReturn(memberA);

        VirtualApplicationResolver.of(
                List.of(newRule("CAFESERVICE", ref("CAFE.A", "SPRING_BOOT"))),
                validator);

        verify(validator).newApplication("CAFE.A", "SPRING_BOOT");
    }

    @Test
    void of_rejects_emptyMembers() {
        ApplicationValidator validator = mock(ApplicationValidator.class);
        VirtualApplicationRule rule = newRule("CAFESERVICE");

        assertThatThrownBy(() -> VirtualApplicationResolver.of(List.of(rule), validator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("members must not be empty");
    }

    @Test
    void of_rejects_emptyVirtualServiceName() {
        ApplicationValidator validator = mock(ApplicationValidator.class);
        VirtualApplicationRule rule = newRule("", ref("CAFE.A", "SPRING_BOOT"));

        assertThatThrownBy(() -> VirtualApplicationResolver.of(List.of(rule), validator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("virtualServiceName must not be empty");
    }

    @Test
    void of_rejects_duplicateVirtualServiceName() {
        ApplicationValidator validator = mock(ApplicationValidator.class);
        when(validator.newApplication("CAFE.A", "SPRING_BOOT"))
                .thenReturn(new Application("CAFE.A", ServiceType.STAND_ALONE));

        VirtualApplicationRule first = newRule("DUP", ref("CAFE.A", "SPRING_BOOT"));
        VirtualApplicationRule second = newRule("DUP", ref("CAFE.A", "SPRING_BOOT"));

        assertThatThrownBy(() -> VirtualApplicationResolver.of(List.of(first, second), validator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate virtualServiceName");
    }

    private static VirtualApplicationRule newRule(String virtualServiceName, ApplicationRef... members) {
        VirtualApplicationRule rule = new VirtualApplicationRule();
        rule.setVirtualServiceName(virtualServiceName);
        rule.setMembers(List.of(members));
        return rule;
    }

    private static ApplicationRef ref(String name, String serviceType) {
        ApplicationRef ref = new ApplicationRef();
        ref.setName(name);
        ref.setServiceType(serviceType);
        return ref;
    }

    private static ServiceType serviceTypeNamed(String name) {
        ServiceType serviceType = mock(ServiceType.class);
        when(serviceType.getName()).thenReturn(name);
        return serviceType;
    }
}