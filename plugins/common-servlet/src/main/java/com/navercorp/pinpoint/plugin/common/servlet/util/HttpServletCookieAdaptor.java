package com.navercorp.pinpoint.plugin.common.servlet.util;

import com.navercorp.pinpoint.bootstrap.plugin.request.CookieAdaptor;
import com.navercorp.pinpoint.common.util.Assert;

import javax.servlet.http.Cookie;

class HttpServletCookieAdaptor implements CookieAdaptor {
    private final Cookie cookie;

    public HttpServletCookieAdaptor(Cookie cookie) {
        this.cookie = Assert.requireNonNull(cookie, "cookie");
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
