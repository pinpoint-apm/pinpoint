/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.plugin.spring.web.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.plugin.spring.web.SpringWebMvcConstants;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author emeroad
 */
public class ResponseBodyObtainInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public ResponseBodyObtainInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(SpringWebMvcConstants.BODY_OBTAIN_SERVICE_TYPE);
    }

    /**
     * 采集请求体
     *
     * @param target    目标方法
     * @param args      拦截方法的参数
     * @param result    拦截方法的返回值，也就是我们要采集的响应体
     * @param throwable 拦截方法的异常
     */
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        Trace trace = traceContext.currentTraceObject();
        try {
            NativeWebRequest nativeRequest = (NativeWebRequest) args[0];
            HttpServletRequest request = (HttpServletRequest) nativeRequest.getNativeRequest();

            if (null == trace) {
                // 此处代表被采样率过滤掉的trace，同时采样策略需要采集此部分调用报文
                if (traceContext.bodyObtainEnable() && traceContext.bodyObtainStrategy() <  PinpointConstants.STRATEGY_2) {
                    Trace rawTraceObject = traceContext.currentRawTraceObject();
                    if (null != rawTraceObject) {
                        recorderWebInfo(rawTraceObject, request, result, throwable);
                    }
                }
                return;
            }

            // 报文采集
            recorderWebInfo(trace, request, result, throwable);

        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("==========pinpoint获取响应体异常====：{}", th.getMessage(), th);
            }
        } finally {
            if (null != trace) {
                trace.traceBlockEnd();
            }
        }
    }

    private void recorderWebInfo(Trace trace, HttpServletRequest request, Object result, Throwable throwable) throws Exception {
        // 可以定义哪些请求要采集
        String method = request.getMethod();
        if (SpringWebMvcConstants.TRACE_METHODS.contains(method)) {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (null != recorder) {
                recorder.recordApi(descriptor);
                recorder.recordException(throwable);
            }

            // 判断是否进行报文采集
            if (this.traceContext.bodyObtainEnable()) {
                SpanRecorder spanRecorder = trace.getSpanRecorder();
                if (null == spanRecorder) {
                    return;
                }
                // 赋值采样策略
                spanRecorder.recordWebInfoStrategy(this.traceContext.bodyObtainStrategy());

                // 采集请求url，url在tomcat插件同样采集了一次，避免某一个插件失效采不到url
                spanRecorder.recordWebInfoRequestUrl(request.getRequestURL().toString());

                // 采集请求参数（@requestParam注解的参数）
                Map<String, String[]> parameterMap = request.getParameterMap();

                // spanRecorder.requestBodyTraced()代表已经在@requestBody处采集了请求体，此处逻辑无需执行
                if (!spanRecorder.requestBodyTraced() && null != parameterMap) {
                    spanRecorder.recordWebInfoRequestBody(parameterMap);
                }

                // 采集响应体
                if (null != result) {
                    spanRecorder.recordWebInfoResponseBody(result);
                }
            }
        }
    }
}