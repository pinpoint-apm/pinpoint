package com.navercorp.pinpoint.plugin.common.servlet.util;

import com.navercorp.pinpoint.bootstrap.plugin.request.CookieAdaptor;

import javax.servlet.http.Cookie;
import java.util.Objects;

class HttpServletCookieAdaptor implements CookieAdaptor {
    private final Cookie cookie;

    public HttpServletCookieAdaptor(Cookie cookie) {
        this.cookie = Objects.requireNonNull(cookie, "cookie");
    }

    @Override
    public String getName() {
        return cookie.getName();
    }

    @Override
    public String getValue() {
        return cookie.getValue();
    }
}
