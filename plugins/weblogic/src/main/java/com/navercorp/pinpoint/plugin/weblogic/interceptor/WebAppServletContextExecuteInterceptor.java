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

package com.navercorp.pinpoint.plugin.weblogic.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerCookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListener;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.weblogic.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.weblogic.WeblogicConfiguration;
import com.navercorp.pinpoint.plugin.weblogic.WeblogicConstants;
import weblogic.servlet.internal.ServletRequestImpl;
import weblogic.servlet.internal.ServletResponseImpl;

/**
 * @author andyspan
 * @author jaehong.kim
 */
public class WebAppServletContextExecuteInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private MethodDescriptor methodDescriptor;
    private final ServletRequestListener<ServletRequestImpl> servletRequestListener;

    public WebAppServletContextExecuteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, RequestRecorderFactory<ServletRequestImpl> requestRecorderFactory) {

        this.methodDescriptor = methodDescriptor;
        final WeblogicConfiguration config = new WeblogicConfiguration(traceContext.getProfilerConfig());
        RequestAdaptor<ServletRequestImpl> requestAdaptor = new ServletRequestImplAdaptor();
        ParameterRecorder<ServletRequestImpl> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());

        ServletRequestListenerBuilder<ServletRequestImpl> builder = new ServletRequestListenerBuilder<ServletRequestImpl>(WeblogicConstants.WEBLOGIC, traceContext, requestAdaptor);
        builder.setExcludeURLFilter(config.getExcludeUrlFilter());
        builder.setParameterRecorder(parameterRecorder);
        builder.setRequestRecorderFactory(requestRecorderFactory);

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        builder.setRealIpSupport(config.getRealIpHeader(), config.getRealIpEmptyValue());
        builder.setHttpStatusCodeRecorder(profilerConfig.getHttpStatusCodeErrors());
        builder.setServerHeaderRecorder(profilerConfig.readList(ServerHeaderRecorder.CONFIG_KEY_RECORD_REQ_HEADERS));
        builder.setServerCookieRecorder(profilerConfig.readList(ServerCookieRecorder.CONFIG_KEY_RECORD_REQ_COOKIES));
        this.servletRequestListener = builder.build();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(args)) {
            return;
        }

        try {
            final ServletRequestImpl request = (ServletRequestImpl) args[0];
            this.servletRequestListener.initialized(request, WeblogicConstants.WEBLOGIC_METHOD, this.methodDescriptor);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to servlet request event handle.", t);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!validate(args)) {
            return;
        }

        try {
            final ServletRequestImpl request = (ServletRequestImpl) args[0];
            final ServletResponseImpl response = (ServletResponseImpl) args[1];
            final int statusCode = getStatusCode(response);
            this.servletRequestListener.destroyed(request, throwable, statusCode);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    private boolean validate(final Object[] args) {
        if (ArrayUtils.getLength(args) < 2) {
            return false;
        }

        if (!(args[0] instanceof ServletRequestImpl)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object, Not implemented of weblogic.servlet.internal.ServletRequestImpl. args[0]={}", args[0]);
            }
            return false;
        }

        if (!(args[1] instanceof ServletResponseImpl)) {
            if (isDebug) {
                logger.debug("Invalid args[1] object, Not implemented of weblogic.servlet.internal.ServletResponseImpl. args[1]={}.", args[1]);
            }
            return false;
        }
        return true;
    }

    private int getStatusCode(final ServletResponseImpl response) {
        try {
            return response.getStatus();
        } catch (Exception ignored) {
        }
        return 0;
    }
}