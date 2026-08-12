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

import org.apache.hc.client5.http.DnsResolver;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookDnsResolverTest {

    @Test
    void resolvePublicAddress() throws Exception {
        InetAddress publicAddress = InetAddress.getByName("8.8.8.8");
        WebhookDnsResolver resolver = new WebhookDnsResolver(new StaticDnsResolver(publicAddress));

        assertThat(resolver.resolve("example.com")).containsExactly(publicAddress);
    }

    @Test
    void rejectPrivateAddress() throws Exception {
        InetAddress privateAddress = InetAddress.getByName("127.0.0.1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(new StaticDnsResolver(privateAddress));

        assertThatThrownBy(() -> resolver.resolve("example.com"))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectPrivateRangeWhenNoHostIsAllowed() throws Exception {
        for (String privateAddress : List.of("10.0.0.1", "172.16.0.1", "192.168.0.1")) {
            WebhookDnsResolver resolver = new WebhookDnsResolver(
                    new StaticDnsResolver(InetAddress.getByName(privateAddress)));

            assertThatThrownBy(() -> resolver.resolve("example.com"))
                    .as("blocked by the default deny-all policy: %s", privateAddress)
                    .isInstanceOf(UnknownHostException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void rejectMixedPublicAndPrivateAddresses() throws Exception {
        InetAddress publicAddress = InetAddress.getByName("8.8.8.8");
        InetAddress privateAddress = InetAddress.getByName("10.0.0.1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(new StaticDnsResolver(publicAddress, privateAddress));

        assertThatThrownBy(() -> resolver.resolve("example.com"))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectNat64PrivateAddress() throws Exception {
        InetAddress nat64LoopbackAddress = InetAddress.getByName("64:ff9b::7f00:1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(new StaticDnsResolver(nat64LoopbackAddress));

        assertThatThrownBy(() -> resolver.resolve("example.com"))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectSpecialPurposeIpv6Address() throws Exception {
        InetAddress teredoAddress = InetAddress.getByName("2001::1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(new StaticDnsResolver(teredoAddress));

        assertThatThrownBy(() -> resolver.resolve("example.com"))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolveAllowedPrivateAddress() throws Exception {
        InetAddress privateAddress = InetAddress.getByName("10.0.0.1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(
                new StaticDnsResolver(privateAddress),
                new WebhookHostPolicy(List.of("api.example.com"), List.of()));

        assertThat(resolver.resolve("api.example.com")).containsExactly(privateAddress);
    }

    @Test
    void resolveAllowedPrivateAddressBySuffix() throws Exception {
        InetAddress privateAddress = InetAddress.getByName("10.0.0.1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(
                new StaticDnsResolver(privateAddress),
                new WebhookHostPolicy(List.of(), List.of(".hooks.example.com")));

        assertThat(resolver.resolve("n8n.hooks.example.com")).containsExactly(privateAddress);
    }

    @Test
    void rejectPrivateAddressForHostOutsideAllowlist() throws Exception {
        InetAddress privateAddress = InetAddress.getByName("10.0.0.1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(
                new StaticDnsResolver(privateAddress),
                new WebhookHostPolicy(List.of("api.example.com"), List.of()));

        assertThatThrownBy(() -> resolver.resolve("evil.example.com"))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectLoopbackEvenForAllowedHost() throws Exception {
        InetAddress loopbackAddress = InetAddress.getByName("127.0.0.1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(
                new StaticDnsResolver(loopbackAddress),
                new WebhookHostPolicy(List.of("api.example.com"), List.of()));

        assertThatThrownBy(() -> resolver.resolve("api.example.com"))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectAlwaysBlockedRangesEvenForAllowedHost() throws Exception {
        WebhookHostPolicy policy = new WebhookHostPolicy(List.of("api.example.com"), List.of());

        for (String blocked : List.of("169.254.169.254", "127.0.0.1", "0.0.0.0")) {
            WebhookDnsResolver resolver = new WebhookDnsResolver(
                    new StaticDnsResolver(InetAddress.getByName(blocked)), policy);

            assertThatThrownBy(() -> resolver.resolve("api.example.com"))
                    .as("blocked regardless of the allowlist: %s", blocked)
                    .isInstanceOf(UnknownHostException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void rejectPrivateAddressForIpLiteralHostEvenWhenListed() throws Exception {
        InetAddress privateAddress = InetAddress.getByName("10.0.0.1");
        WebhookDnsResolver resolver = new WebhookDnsResolver(
                new StaticDnsResolver(privateAddress),
                new WebhookHostPolicy(List.of("10.0.0.1"), List.of()));

        assertThatThrownBy(() -> resolver.resolve("10.0.0.1"))
                .isInstanceOf(UnknownHostException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolveCanonicalHostname() throws Exception {
        WebhookDnsResolver resolver = new WebhookDnsResolver(new StaticDnsResolver(InetAddress.getByName("8.8.8.8")));

        assertThat(resolver.resolveCanonicalHostname("example.com")).isEqualTo("canonical.example.com");
    }

    private static class StaticDnsResolver implements DnsResolver {
        private final InetAddress[] addresses;

        private StaticDnsResolver(InetAddress... addresses) {
            this.addresses = addresses;
        }

        @Override
        public InetAddress[] resolve(String host) {
            return addresses;
        }

        @Override
        public String resolveCanonicalHostname(String host) {
            return "canonical." + host;
        }
    }
}
