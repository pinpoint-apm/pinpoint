package com.navercorp.pinpoint.plugin.httpclient5;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.net.URIAuthority;

import java.net.URI;
import java.net.URISyntaxException;

public class HostUtils {

    public static String get(final HttpHost httpHost, final HttpRequest httpRequest) {
        if (httpHost != null) {
            return HostAndPort.toHostAndPortString(httpHost.getHostName(), httpHost.getPort());
        }
        return get(httpRequest);
    }

    public static String get(final HttpRequest httpRequest) {
        if (httpRequest != null) {
            final URIAuthority authority = httpRequest.getAuthority();
            if (authority != null) {
                return HostAndPort.toHostAndPortString(authority.getHostName(), authority.getPort());
            }
            try {
                final URI requestURI = httpRequest.getUri();
                if (requestURI.isAbsolute()) {
                    return HostAndPort.toHostAndPortString(requestURI.getHost(), requestURI.getPort());
                }
            } catch (URISyntaxException ignored) {
            }
        }
        return "UNKNOWN";
    }
}
