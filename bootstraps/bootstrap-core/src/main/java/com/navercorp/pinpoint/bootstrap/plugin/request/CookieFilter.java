package com.navercorp.pinpoint.bootstrap.plugin.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CookieFilter<C> {

    public List<CookieAdaptor> wrap(C[] cookies) {
        if (cookies == null) {
            return Collections.emptyList();
        }

        final List<CookieAdaptor> result = new ArrayList<CookieAdaptor>(cookies.length);
        for (C cookie : cookies) {
            result.add(newCookieAdaptor(cookie));
        }
        return result;
    }

    public List<CookieAdaptor> filter(C[] cookies, String[] cookieNames) {
        if (cookies == null) {
            return Collections.emptyList();
        }

        List<CookieAdaptor> result = null;
        for (C cookie : cookies) {
            final String name = getName(cookie);
            if (isMatch(cookieNames, name)) {
                if (result == null) {
                    result = new ArrayList<CookieAdaptor>(cookieNames.length);
                }
                result.add(newCookieAdaptor(cookie));
            }
        }
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public boolean isMatch(String[] cookieNames, String name) {
        for (String cookieName : cookieNames) {
            if (cookieName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    protected abstract CookieAdaptor newCookieAdaptor(C cookie);

    protected abstract String getName(C cookie);

}
