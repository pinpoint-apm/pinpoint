/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.elasticsearch.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.plugin.elasticsearch.ElasticPluginUtil.getIndex;
import static com.navercorp.pinpoint.plugin.elasticsearch.ElasticsearchConstants.*;
import static com.navercorp.pinpoint.plugin.elasticsearch.interceptor.HighLevelConnectInterceptor.merge;

/**
 * @author shuijing
 */
public class RestClient7ConnectInterceptor implements AroundInterceptor {


    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public RestClient7ConnectInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (null != trace) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(ELASTICSEARCH_EEST);
            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            recorder.recordDestinationId("ElasticSearch");
        } else {
            logger.debug("before(): no trace found, set new trace");
            trace = traceContext.newTraceObject();
            SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(ELASTICSEARCH_EEST);
            recorder.attachFrameObject(true);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null || !trace.canSampled()) {
            return;
        }

        try {
            if (isNewTrace(trace.getSpanRecorder().getFrameObject())) {
                //new Trace create
                SpanRecorder recorder = trace.getSpanRecorder();
                recorder.recordApi(descriptor);
                recorder.recordEndPoint(merge(getEndpoints(target)));
                recorder.recordAttribute(INDEX_KEY, getRequestEndpoint(args));
                recorder.recordAttribute(INDEX_KEY_METHOD, getRequestMethod(args));
                if (throwable != null) {
                    logger.debug("after(): get throwable");
                    recorder.recordException(throwable);
                }
            } else {
                SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordApi(descriptor);
                recorder.recordDestinationId("ElasticSearch");
                recorder.recordEndPoint(merge(getEndpoints(target)));
                recorder.recordAttribute(INDEX_KEY, getRequestEndpoint(args));
                recorder.recordAttribute(INDEX_KEY_METHOD, getRequestMethod(args));
                if (throwable != null) {
                    logger.debug("after(): get throwable");
                    recorder.recordException(throwable);
                }
            }
        } catch (Exception e) {
            logger.error("after(): get throwable");
        } finally {
            if (isNewTrace(trace.getSpanRecorder().getFrameObject())) {
                traceContext.removeTraceObject();
                trace.close();
            } else {
                trace.traceBlockEnd();
            }
        }
    }

    private boolean isNewTrace(Object object) {
        return object != null && (Boolean) object;
    }

    protected List<String> getEndpoints(Object target) {
        final List<String> hostList = new ArrayList<String>();
        try {
            if (target instanceof org.elasticsearch.client.RestClient) {
                List<Node> nodes = ((RestClient) target).getNodes();
                if (nodes != null) {
                    for (Node node : nodes) {
                        final String hostAddress = HostAndPort.toHostAndPortString(node.getHost().getHostName(), node.getHost().getPort());
                        hostList.add(hostAddress);
                    }
                }
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.info("failed to getEndpoints method. caused:{}", e.getMessage(), e);
            }
        }
        return hostList;
    }

    protected String getRequestEndpoint(Object[] args) {
        try {
            if (args.length > 0) {
                if (args[0] instanceof org.elasticsearch.client.Request) {
                    Request request = ((Request) args[0]);
                    String endpoint = request.getEndpoint();
                    if (endpoint != null) {
                        return getIndex((endpoint));
                    }
                }
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.info("failed to getRequestEndpoint method. caused:{}", e.getMessage(), e);
            }
        }
        return "";
    }


    protected String getRequestMethod(Object[] args) {
        try {
            if (args.length > 0) {
                if (args[0] instanceof org.elasticsearch.client.Request) {
                    Request request = ((Request) args[0]);
                    String method = request.getMethod();
                    if (method != null) {
                        return method;
                    }
                }
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.info("failed to getRequestMethod method. caused:{}", e.getMessage(), e);
            }
        }
        return "";
    }





}