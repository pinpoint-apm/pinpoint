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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Rejects a webhook host whose resolved addresses are not permitted, which is the point where SSRF
 * is actually enforced: the check runs against the addresses the connection will use, so a host
 * that passed validation at registration cannot be re-pointed at an internal address later
 * (DNS rebinding).
 * <p>
 * The webhook HTTP client must also disable redirect handling, otherwise a permitted host can hand
 * out a 30x pointing at an internal address.
 */
public class WebhookDnsResolver implements DnsResolver {

    private final DnsResolver delegate;
    private final WebhookHostPolicy hostPolicy;

    public WebhookDnsResolver(DnsResolver delegate) {
        this(delegate, WebhookHostPolicy.denyAll());
    }

    public WebhookDnsResolver(DnsResolver delegate, WebhookHostPolicy hostPolicy) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.hostPolicy = Objects.requireNonNull(hostPolicy, "hostPolicy");
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = delegate.resolve(host);
        if (addresses == null || addresses.length == 0) {
            throw new UnknownHostException(host);
        }

        for (InetAddress address : addresses) {
            validate(host, address);
        }
        return addresses;
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
        return delegate.resolveCanonicalHostname(host);
    }

    private void validate(String host, InetAddress address) throws UnknownHostException {
        try {
            WebhookUrlValidator.validateResolvedAddress(host, address, hostPolicy);
        } catch (IllegalArgumentException e) {
            UnknownHostException exception = new UnknownHostException(
                    "Webhook host resolves to a non-public address. host=" + host + ", address=" + address.getHostAddress()
            );
            exception.initCause(e);
            throw exception;
        }
    }
}
