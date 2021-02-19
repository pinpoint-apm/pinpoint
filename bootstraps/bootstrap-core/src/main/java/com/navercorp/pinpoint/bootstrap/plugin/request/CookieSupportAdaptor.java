package com.navercorp.pinpoint.bootstrap.plugin.request;

import java.util.List;

public interface CookieSupportAdaptor<REQ> {

    List<CookieAdaptor> getCookie(REQ request);

    List<CookieAdaptor> getCookie(REQ request, String[] cookieNames);

}
