package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringStringValue;

import java.util.List;

public class DefaultServerCookieRecorder<REQ> implements ServerCookieRecorder<REQ> {

    private final CookieSupportAdaptor<REQ> requestAdaptor;

    private final String[] recordCookies;

    public DefaultServerCookieRecorder(CookieSupportAdaptor<REQ> requestAdaptor, List<String> recordCookies) {
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");

        Assert.requireNonNull(recordCookies, "recordCookies");
        this.recordCookies = recordCookies.toArray(new String[0]);
    }


    @Override
    public void recordCookie(final SpanRecorder recorder, final REQ request) {
        List<CookieAdaptor> cookieList = requestAdaptor.getCookie(request, recordCookies);
        if (CollectionUtils.isEmpty(cookieList)) {
            return;
        }
        for (CookieAdaptor cookieAdaptor : cookieList) {
            StringStringValue cookie = new StringStringValue(cookieAdaptor.getName(), cookieAdaptor.getValue());
            recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, cookie);
        }
    }

}
