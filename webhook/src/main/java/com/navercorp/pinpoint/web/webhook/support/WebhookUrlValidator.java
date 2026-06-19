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

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv4.IPv4Address;
import inet.ipaddr.ipv6.IPv6Address;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

public final class WebhookUrlValidator {

    private static final int MAX_PORT = 65535;
    private static final List<IPAddress> BLOCKED_IPV4_RANGES = toPrefixBlocks(List.of(
            "0.0.0.0/8",
            "10.0.0.0/8",
            "127.0.0.0/8",
            "100.64.0.0/10",
            "169.254.0.0/16",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "192.0.0.0/24",
            "192.0.2.0/24",
            "192.88.99.0/24",
            "198.18.0.0/15",
            "198.51.100.0/24",
            "203.0.113.0/24",
            "224.0.0.0/4",
            "240.0.0.0/4"
    ));
    private static final List<IPAddress> BLOCKED_IPV6_RANGES = toPrefixBlocks(List.of(
            "::/96",
            "64:ff9b:1::/48",
            "fc00::/7",
            "100::/64",
            "100:0:0:1::/64",
            "2001::/23",
            "2001:db8::/32",
            "2002::/16",
            "3fff::/20",
            "5f00::/16"
    ));

    private WebhookUrlValidator() {
    }

    public static String validate(String url) {
        final URI uri = validateUriSyntax(url);
        validateHostWithoutResolving(uri.getHost());

        return uri.toString();
    }

    public static String validateSyntax(String url) {
        return validateUriSyntax(url).toString();
    }

    private static URI validateUriSyntax(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Webhook URL is required");
        }

        final URI uri;

        try {
            uri = new URI(url).normalize().parseServerAuthority();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed webhook URL", e);
        }

        validateScheme(uri);
        validateAuthority(uri);

        return uri;
    }

    private static void validateScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("Webhook URL scheme is required");
        }

        String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
            throw new IllegalArgumentException("Webhook URL scheme must be http or https");
        }
    }

    private static void validateAuthority(URI uri) {
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("Webhook URL host is required");
        }
        if (isBlockedHostLiteral(uri.getHost())) {
            throw new IllegalArgumentException("Webhook URL host is not allowed");
        }
        if (uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("Webhook URL user info is not allowed");
        }
        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException("Webhook URL fragment is not allowed");
        }
        int port = uri.getPort();
        if (port == -1 && hasExplicitPort(uri)) {
            throw new IllegalArgumentException("Webhook URL port is not valid");
        }
        if (port == 0 || port > MAX_PORT) {
            throw new IllegalArgumentException("Webhook URL port is not allowed");
        }
    }

    private static boolean hasExplicitPort(URI uri) {
        String rawAuthority = uri.getRawAuthority();
        if (rawAuthority == null || rawAuthority.isEmpty()) {
            return false;
        }

        int hostStartIndex = rawAuthority.lastIndexOf('@') + 1;
        if (rawAuthority.charAt(hostStartIndex) == '[') {
            int hostEndIndex = rawAuthority.indexOf(']', hostStartIndex);
            return hostEndIndex >= 0
                    && hostEndIndex + 1 < rawAuthority.length()
                    && rawAuthority.charAt(hostEndIndex + 1) == ':';
        }
        return rawAuthority.indexOf(':', hostStartIndex) >= 0;
    }

    private static void validateHostWithoutResolving(String host) {
        String normalizedHost = normalizeHost(host);
        if (isBlockedHostName(normalizedHost)) {
            throw new IllegalArgumentException("Webhook URL host is not allowed");
        }

        IPAddress address = toHostLiteralAddress(normalizedHost);
        if (address != null) {
            validateResolvedAddress(address.toInetAddress());
        }
    }

    public static void validateResolvedAddress(InetAddress address) {
        if (address == null) {
            throw new IllegalArgumentException("Webhook URL resolved address is required");
        }
        if (isBlockedAddress(address)) {
            throw new IllegalArgumentException("Webhook URL resolves to a non-public address");
        }
    }

    private static String normalizeHost(String host) {
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        while (normalizedHost.endsWith(".")) {
            normalizedHost = normalizedHost.substring(0, normalizedHost.length() - 1);
        }
        return normalizedHost;
    }

    private static boolean isBlockedHostName(String host) {
        return host.equals("localhost")
                || host.endsWith(".localhost")
                || host.equals("local")
                || host.endsWith(".local");
    }

    private static boolean isBlockedHostLiteral(String host) {
        IPAddress address = toHostLiteralAddress(host);
        return address != null && isIpv4MappedIpv6(address);
    }

    private static IPAddress toHostLiteralAddress(String host) {
        String literal = unwrapIpv6Literal(host);
        if (!isLikelyIpLiteral(literal)) {
            return null;
        }
        return new IPAddressString(literal).getAddress();
    }

    private static boolean isLikelyIpLiteral(String host) {
        return host.indexOf(':') >= 0 || (!host.isEmpty() && Character.isDigit(host.charAt(0)));
    }

    private static String unwrapIpv6Literal(String host) {
        if (host.length() >= 2 && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            return host.substring(1, host.length() - 1);
        }
        return host;
    }

    private static boolean isIpv4MappedIpv6(IPAddress address) {
        return address.isIPv6() && address.toIPv6().isIPv4Mapped();
    }

    private static boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        IPAddress ipAddress = toIpAddress(address);
        if (ipAddress == null) {
            return true;
        }

        if (ipAddress.isIPv4()) {
            return contains(BLOCKED_IPV4_RANGES, ipAddress);
        }
        if (ipAddress.isIPv6()) {
            return isBlockedIpv6(ipAddress.toIPv6());
        }

        return true;
    }

    private static boolean isBlockedIpv6(IPv6Address address) {
        if (address.isIPv4Mapped()) {
            return true;
        }
        if (address.isIPv4Translatable()) {
            return true;
        }
        if (address.isWellKnownIPv4Translatable()) {
            return contains(BLOCKED_IPV4_RANGES, address.getEmbeddedIPv4Address());
        }
        return contains(BLOCKED_IPV6_RANGES, address);
    }

    private static List<IPAddress> toPrefixBlocks(List<String> cidrs) {
        return cidrs.stream()
                .map(WebhookUrlValidator::toPrefixBlock)
                .toList();
    }

    private static IPAddress toPrefixBlock(String cidr) {
        IPAddress address = new IPAddressString(cidr).getAddress();
        if (address == null) {
            throw new IllegalStateException("Invalid blocked address range: " + cidr);
        }
        return address.toPrefixBlock();
    }

    private static IPAddress toIpAddress(InetAddress address) {
        if (address instanceof Inet4Address inet4Address) {
            return new IPv4Address(inet4Address);
        }
        if (address instanceof Inet6Address inet6Address) {
            return new IPv6Address(inet6Address);
        }
        return null;
    }

    private static boolean contains(List<IPAddress> ranges, IPAddress address) {
        if (address == null) {
            return true;
        }
        for (IPAddress range : ranges) {
            if (range.contains(address)) {
                return true;
            }
        }
        return false;
    }
}
