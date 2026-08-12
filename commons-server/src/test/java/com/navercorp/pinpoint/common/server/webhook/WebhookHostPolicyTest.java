/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.webhook;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookHostPolicyTest {

    @Test
    void denyAllAllowsNothing() {
        WebhookHostPolicy policy = WebhookHostPolicy.denyAll();

        assertThat(policy.isAllowed("api.example.com")).isFalse();
        assertThat(policy.isAllowed("10.0.0.1")).isFalse();
    }

    @Test
    void allowExactHost() {
        WebhookHostPolicy policy = new WebhookHostPolicy(List.of("api.example.com"), List.of());

        assertThat(policy.isAllowed("api.example.com")).isTrue();
        assertThat(policy.isAllowed("other.example.com")).isFalse();
        assertThat(policy.isAllowed("evil-api.example.com")).isFalse();
    }

    @Test
    void allowHostSuffix() {
        WebhookHostPolicy policy = new WebhookHostPolicy(List.of(), List.of(".hooks.example.com"));

        assertThat(policy.isAllowed("n8n.hooks.example.com")).isTrue();
        assertThat(policy.isAllowed("a.b.hooks.example.com")).isTrue();
        assertThat(policy.isAllowed("hooks.example.com")).isFalse();
        assertThat(policy.isAllowed("other.example.com")).isFalse();
    }

    @Test
    void suffixWithoutLeadingDotDoesNotMatchPartialLabel() {
        WebhookHostPolicy policy = new WebhookHostPolicy(List.of(), List.of("hooks.example.com"));

        assertThat(policy.isAllowed("n8n.hooks.example.com")).isTrue();
        assertThat(policy.isAllowed("evil-hooks.example.com")).isFalse();
    }

    @Test
    void normalizeCaseAndTrailingDotAndWhitespace() {
        WebhookHostPolicy policy = new WebhookHostPolicy(
                List.of(" API.Example.COM. "), List.of(" .Hooks.Example.COM. "));

        assertThat(policy.isAllowed("api.example.com")).isTrue();
        assertThat(policy.isAllowed("API.EXAMPLE.COM")).isTrue();
        assertThat(policy.isAllowed("api.example.com.")).isTrue();
        assertThat(policy.isAllowed("n8n.hooks.example.com.")).isTrue();
    }

    @Test
    void ignoreBlankEntries() {
        WebhookHostPolicy policy = new WebhookHostPolicy(List.of("", "  "), List.of("", "  "));

        assertThat(policy.isAllowed("api.example.com")).isFalse();
        assertThat(policy.isAllowed("")).isFalse();
    }

    @Test
    void nullInputsAreTreatedAsEmpty() {
        WebhookHostPolicy policy = new WebhookHostPolicy(null, null);

        assertThat(policy.isAllowed("api.example.com")).isFalse();
        assertThat(policy.isAllowed(null)).isFalse();
    }
}
