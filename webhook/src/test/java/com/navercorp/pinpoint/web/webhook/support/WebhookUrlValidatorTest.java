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
package com.navercorp.pinpoint.web.webhook.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookUrlValidatorTest {

    @Test
    void validatePublicHttpUrl() {
        assertThat(WebhookUrlValidator.validate("https://8.8.8.8/webhook?token=test"))
                .isEqualTo("https://8.8.8.8/webhook?token=test");
    }

    @Test
    void validatePublicIpv6Url() {
        assertThat(WebhookUrlValidator.validate("https://[2606:4700:4700::1111]/webhook"))
                .isEqualTo("https://[2606:4700:4700::1111]/webhook");
    }

    @Test
    void validatePublicNat64Ipv6Url() {
        assertThat(WebhookUrlValidator.validate("https://[64:ff9b::808:808]/webhook"))
                .isEqualTo("https://[64:ff9b::808:808]/webhook");
    }

    @Test
    void validateWithoutResolvingHost() {
        assertThat(WebhookUrlValidator.validate("https://not-resolved.invalid/webhook"))
                .isEqualTo("https://not-resolved.invalid/webhook");
    }

    @Test
    void validateSyntaxWithoutResolvingHost() {
        assertThat(WebhookUrlValidator.validateSyntax("https://127.0.0.1/webhook"))
                .isEqualTo("https://127.0.0.1/webhook");
        assertThat(WebhookUrlValidator.validateSyntax("https://not-resolved.invalid/webhook"))
                .isEqualTo("https://not-resolved.invalid/webhook");
    }

    @Test
    void validateSyntaxRejectsMalformedUrl() {
        assertInvalidSyntax("ftp://8.8.8.8/webhook");
        assertInvalidSyntax("https://user:password@8.8.8.8/webhook");
        assertInvalidSyntax("https://8.8.8.8/webhook#fragment");
        assertInvalidSyntax("https://8.8.8.8:70000/webhook");
    }

    @Test
    void rejectEmptyUrl() {
        assertInvalid(null);
        assertInvalid("");
        assertInvalid("   ");
        assertInvalidSyntax(null);
        assertInvalidSyntax("");
        assertInvalidSyntax("   ");
    }

    @Test
    void rejectUnsupportedScheme() {
        assertInvalid("file:///etc/passwd");
        assertInvalid("ftp://8.8.8.8/webhook");
    }

    @Test
    void rejectLocalhost() {
        assertInvalid("http://localhost/webhook");
        assertInvalid("http://localhost./webhook");
        assertInvalid("http://service.local/webhook");
    }

    @Test
    void rejectPrivateAndLocalIpv4Address() {
        assertInvalid("http://127.0.0.1/webhook");
        assertInvalid("http://10.0.0.1/webhook");
        assertInvalid("http://172.16.0.1/webhook");
        assertInvalid("http://192.168.0.1/webhook");
        assertInvalid("http://192.0.2.1/webhook");
        assertInvalid("http://169.254.169.254/latest/meta-data");
        assertInvalid("http://240.0.0.1/webhook");
    }

    @Test
    void rejectPrivateAndLocalIpv6Address() {
        assertInvalid("http://[::1]/webhook");
        assertInvalid("http://[fc00::1]/webhook");
        assertInvalid("http://[fe80::1]/webhook");
        assertInvalid("http://[fec0::1]/webhook");
        assertInvalid("http://[::ffff:0:127.0.0.1]/webhook");
    }

    @Test
    void rejectIpv4MappedIpv6Address() {
        assertInvalid("http://[::ffff:127.0.0.1]/webhook");
        assertInvalid("http://[::ffff:8.8.8.8]/webhook");
        assertInvalidSyntax("http://[::ffff:8.8.8.8]/webhook");
    }

    @Test
    void rejectNat64PrivateAndLocalIpv6Address() {
        assertInvalid("http://[64:ff9b::7f00:1]/webhook");
        assertInvalid("http://[64:ff9b::a00:1]/webhook");
        assertInvalid("http://[64:ff9b::c0a8:1]/webhook");
        assertInvalid("http://[64:ff9b::a9fe:a9fe]/latest/meta-data");
        assertInvalid("http://[64:ff9b:1::808:808]/webhook");
    }

    @Test
    void rejectSpecialPurposeIpv6Address() {
        assertInvalid("http://[100::]/webhook");
        assertInvalid("http://[100::2]/webhook");
        assertInvalid("http://[100:0:0:1::]/webhook");
        assertInvalid("http://[100:0:0:1::2]/webhook");
        assertInvalid("http://[2001::1]/webhook");
        assertInvalid("http://[2001:2::1]/webhook");
        assertInvalid("http://[2001:10::1]/webhook");
        assertInvalid("http://[2001:db8::1]/webhook");
        assertInvalid("http://[2002::1]/webhook");
        assertInvalid("http://[3fff::1]/webhook");
        assertInvalid("http://[5f00::1]/webhook");
    }

    @Test
    void rejectUserInfoAndFragment() {
        assertInvalid("https://user:password@8.8.8.8/webhook");
        assertInvalid("https://8.8.8.8/webhook#fragment");
        assertInvalid("https://8.8.8.8:70000/webhook");
    }

    private void assertInvalid(String url) {
        assertThatThrownBy(() -> WebhookUrlValidator.validate(url))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void assertInvalidSyntax(String url) {
        assertThatThrownBy(() -> WebhookUrlValidator.validateSyntax(url))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
