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
package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.web.webhook.support.WebhookUrlValidator;
import org.apache.hc.client5.http.DnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class WebhookDnsResolver implements DnsResolver {

    private final DnsResolver delegate;

    public WebhookDnsResolver(DnsResolver delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
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

    private static void validate(String host, InetAddress address) throws UnknownHostException {
        try {
            WebhookUrlValidator.validateResolvedAddress(address);
        } catch (IllegalArgumentException e) {
            UnknownHostException exception = new UnknownHostException(
                    "Webhook host resolves to a non-public address. host=" + host + ", address=" + address.getHostAddress()
            );
            exception.initCause(e);
            throw exception;
        }
    }
}
