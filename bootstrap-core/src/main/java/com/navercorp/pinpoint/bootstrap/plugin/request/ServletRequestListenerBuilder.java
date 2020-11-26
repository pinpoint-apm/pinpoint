/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.HttpStatusCodeErrors;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.DisableRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.DisableParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.bootstrap.plugin.uri.DisabledUriStatRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorder;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServletRequestListenerBuilder<REQ> {
    private final ServiceType serviceType;
    private final TraceContext traceContext;
    private final RequestAdaptor<REQ> requestAdaptor;

    private String realIpHeader;
    private String realIpEmptyValue;

    private ParameterRecorder<REQ> parameterRecorder;

    private Filter<String> excludeUrlFilter;
    private RequestRecorderFactory<REQ> requestRecorderFactory;

    private HttpStatusCodeErrors httpStatusCodeErrors;

    private List<String> recordRequestHeaders;
    private List<String> recordRequestCookies;
    private UriStatRecorder<REQ> uriStatRecorder = DisabledUriStatRecorder.create();

    public ServletRequestListenerBuilder(final ServiceType serviceType,
                                         final TraceContext traceContext,
                                         final RequestAdaptor<REQ> requestAdaptor) {
        this.serviceType = Assert.requireNonNull(serviceType, "serviceType");

        this.traceContext = Assert.requireNonNull(traceContext, "traceContext");
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");
    }



    public void setParameterRecorder(ParameterRecorder<REQ> parameterRecorder) {
        this.parameterRecorder = parameterRecorder;
    }

    public void setRealIpSupport(String realIpHeader, String realIpEmptyValue) {
        this.realIpHeader = realIpHeader;
        this.realIpEmptyValue = realIpEmptyValue;
    }

    public void setExcludeURLFilter(Filter<String> excludeUrlFilter) {
        this.excludeUrlFilter = excludeUrlFilter;
    }

    public void setRequestRecorderFactory(RequestRecorderFactory<REQ> requestRecorderFactory) {
        this.requestRecorderFactory = requestRecorderFactory;
    }

    // refactor~~
    public void setHttpStatusCodeRecorder(final HttpStatusCodeErrors httpStatusCodeErrors) {
        this.httpStatusCodeErrors = httpStatusCodeErrors;
    }

    public void setServerHeaderRecorder(List<String> recordRequestHeaders) {
        this.recordRequestHeaders = recordRequestHeaders;
    }

    public void setServerCookieRecorder(List<String> recordRequestCookies) {
        this.recordRequestCookies = recordRequestCookies;
    }

    public void setReqUriStatRecorder(UriStatRecorder<REQ> reqUriStatRecorder) {
        Assert.requireNonNull(reqUriStatRecorder, "reqUriStatRecorder");
        this.uriStatRecorder = reqUriStatRecorder;
    }

    private <T> Filter<T> newExcludeUrlFilter(Filter<T> excludeUrlFilter) {
        if (excludeUrlFilter == null) {
            return new SkipFilter<T>();
        }
        return excludeUrlFilter;
    }


    public ServletRequestListener<REQ> build() {

        RequestAdaptor<REQ> requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(this.requestAdaptor, realIpHeader, realIpEmptyValue);

        RequestTraceReader<REQ> requestTraceReader = new RequestTraceReader<REQ>(traceContext, requestAdaptor, true);


        ProxyRequestRecorder<REQ> proxyRequestRecorder;
        if (requestRecorderFactory == null) {
            proxyRequestRecorder = new DisableRequestRecorder<REQ>();
        } else {
            proxyRequestRecorder = requestRecorderFactory.getProxyRequestRecorder(requestAdaptor);
        }


        Filter<String> excludeUrlFilter = newExcludeUrlFilter(this.excludeUrlFilter);

        final ServerRequestRecorder<REQ> serverRequestRecorder = newServerRequestRecorder(requestAdaptor);

        ParameterRecorder<REQ> parameterRecorder = newParameterRecorder();

        // not general api : http??
        HttpStatusCodeRecorder httpStatusCodeRecorder;
        if (httpStatusCodeErrors == null) {
            HttpStatusCodeErrors httpStatusCodeErrors = new HttpStatusCodeErrors(Collections.<String>emptyList());
            httpStatusCodeRecorder = new HttpStatusCodeRecorder(httpStatusCodeErrors);
        } else {
            httpStatusCodeRecorder = new HttpStatusCodeRecorder(httpStatusCodeErrors);
        }


        return new ServletRequestListener<REQ>(serviceType, traceContext, requestAdaptor, requestTraceReader,
                excludeUrlFilter, parameterRecorder, proxyRequestRecorder, serverRequestRecorder, httpStatusCodeRecorder, uriStatRecorder);
    }


    private ServerRequestRecorder<REQ> newServerRequestRecorder(RequestAdaptor<REQ> requestAdaptor) {
        final ServerHeaderRecorder<REQ> headerRecorder = newServerHeaderRecorder(requestAdaptor);
        final ServerCookieRecorder<REQ> cookieRecorder = newServerCookieRecorder(requestAdaptor);
        return new ServerRequestRecorder<REQ>(requestAdaptor, headerRecorder, cookieRecorder);
    }

    private ServerHeaderRecorder<REQ> newServerHeaderRecorder(RequestAdaptor<REQ> requestAdaptor) {
        if (CollectionUtils.isEmpty(recordRequestHeaders)) {
            return new BypassServerHeaderRecorder<REQ>();
        }
        return new DefaultServerHeaderRecorder<REQ>(requestAdaptor, recordRequestHeaders);
    }

    private ServerCookieRecorder<REQ> newServerCookieRecorder(RequestAdaptor<REQ> requestAdaptor) {
        if (CollectionUtils.isEmpty(recordRequestCookies)) {
            return new BypassServerCookieRecorder<REQ>();
        }

        if (!(requestAdaptor instanceof CookieSupportAdaptor)) {
            // unsupported
            return new BypassServerCookieRecorder<REQ>();
        }
        CookieSupportAdaptor<REQ> cookieAdaptor = (CookieSupportAdaptor<REQ>) requestAdaptor;
        return new DefaultServerCookieRecorder<REQ>(cookieAdaptor, recordRequestCookies);
    }

    private ParameterRecorder<REQ> newParameterRecorder() {
        if (this.parameterRecorder == null) {
            return new DisableParameterRecorder<REQ>();
        }
        return this.parameterRecorder;
    }
}
