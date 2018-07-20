/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.cxf.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConfig;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConstants;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author barney
 *
 */
public class CxfClientInvokeSyncMethodInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final CxfPluginConfig pluginConfig;

    private final Pattern hiddenParamPattern = Pattern.compile("(.*):([0-9]+)");

    public CxfClientInvokeSyncMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.pluginConfig = new CxfPluginConfig(traceContext.getProfilerConfig());
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        if (!trace.canSampled()) {
            if (isDebug) {
                logger.debug("Sampling is disabled");
            }
            return;
        }

        String endpoint = getDestination(args);
        String operation = getOperation(args);
        Object[] parameters = getParameters(operation, args);

        SpanEventRecorder recorder = trace.traceBlockBegin();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        recorder.recordDestinationId(endpoint);
        recorder.recordAttribute(CxfPluginConstants.CXF_OPERATION, operation);
        recorder.recordAttribute(CxfPluginConstants.CXF_ARGS, Arrays.toString(parameters));
    }

    private String getDestination(Object[] args) {
        String operationInfo = args[1].toString();
        int start = operationInfo.indexOf('{');
        int end = operationInfo.indexOf('}');
        if(start < 0 || end < 0) {
            return operationInfo;
        }
        return operationInfo.substring(start + 1, end);
    }

    private String getOperation(Object[] args) {
        String operationInfo = args[1].toString();
        int start = operationInfo.indexOf('{');
        if (start < 0) {
            return operationInfo;
        }
        return operationInfo.substring(start, operationInfo.length() - 1);
    }

    private Object[] getParameters(String operation, Object[] args) {
        Object[] orgParams = (args[2] == null) ? null : (Object[]) args[2];
        if (orgParams == null) {
            return null;
        }

        String[] hiddenParams = pluginConfig.getClientHiddenParams();
        if (ArrayUtils.isEmpty(hiddenParams)) {
            return orgParams;
        }
        Object[] params = Arrays.copyOf(orgParams, orgParams.length);
        for (String op : hiddenParams) {
            Matcher matcher = hiddenParamPattern.matcher(op);
            if (matcher.matches()) {
                if (operation.equals(matcher.group(1))) {
                    String group = matcher.group(2);
                    int idx = Integer.parseInt(group);
                    if (idx >= params.length) {
                        continue;
                    }
                    params[idx] = "[HIDDEN PARAM]";
                }
            } else {
                if (op.equals(operation)) {
                    return new Object[] { "HIDDEN " + params.length + " PARAM" };
                }
            }
        }
        return params;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

}
