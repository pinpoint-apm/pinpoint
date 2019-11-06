/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.websphere.interceptor;

import com.ibm.websphere.servlet.request.IRequest;
import com.ibm.websphere.servlet.response.IResponse;
import com.ibm.ws.webcontainer.channel.WCCResponseImpl;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerInterceptorHelper;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.plugin.common.servlet.util.ArgumentValidator;
import com.navercorp.pinpoint.plugin.common.servlet.util.ServletArgumentValidator;
import com.navercorp.pinpoint.plugin.websphere.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.websphere.StatusCodeAccessor;
import com.navercorp.pinpoint.plugin.websphere.WebsphereConfiguration;
import com.navercorp.pinpoint.plugin.websphere.WebsphereConstants;


/**
 * @author sjmittal
 * @author jaehong.kim
 */
public class WebContainerHandleRequestInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor methodDescriptor;
    private final ArgumentValidator argumentValidator;
    private final ServletRequestListenerInterceptorHelper<IRequest> servletRequestListenerInterceptorHelper;

    public WebContainerHandleRequestInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory<IRequest> requestRecorderFactory) {
        this.methodDescriptor = descriptor;
        this.argumentValidator = new ServletArgumentValidator(logger, 0, IRequest.class, 1, IResponse.class);
        final WebsphereConfiguration config = new WebsphereConfiguration(traceContext.getProfilerConfig());
        RequestAdaptor<IRequest> requestAdaptor = new IRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, config.getRealIpHeader(), config.getRealIpEmptyValue());
        final ParameterRecorder<IRequest> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
        this.servletRequestListenerInterceptorHelper = new ServletRequestListenerInterceptorHelper<IRequest>(WebsphereConstants.WEBSPHERE, traceContext, requestAdaptor, config.getExcludeUrlFilter(), parameterRecorder, requestRecorderFactory);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!argumentValidator.validate(args)) {
            return;
        }

        try {
            final IRequest request = (IRequest) args[0];
            this.servletRequestListenerInterceptorHelper.initialized(request, WebsphereConstants.WEBSPHERE_METHOD, this.methodDescriptor);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!argumentValidator.validate(args)) {
            return;
        }

        try {
            final IRequest request = (IRequest) args[0];
            final IResponse response = (IResponse) args[1];
            final int statusCode = getStatusCode(response);
            this.servletRequestListenerInterceptorHelper.destroyed(request, throwable, statusCode);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }


    private int getStatusCode(final IResponse response) {
        try {
            if (response instanceof StatusCodeAccessor) {
                final StatusCodeAccessor accessor = (StatusCodeAccessor) response;
                return accessor._$PINPOINT$_getStatusCode();
            } else if (response instanceof WCCResponseImpl) {
                WCCResponseImpl r = (WCCResponseImpl) response;
                return r.getHttpResponse().getStatusCodeAsInt();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}