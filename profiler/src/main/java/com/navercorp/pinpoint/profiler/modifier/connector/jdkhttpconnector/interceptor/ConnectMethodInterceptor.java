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

package com.navercorp.pinpoint.profiler.modifier.connector.jdkhttpconnector.interceptor;

import java.net.HttpURLConnection;
import java.net.URL;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author netspider
 * @author emeroad
 */
public class ConnectMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass()    ;
	private final boolean isDebug = logger.isDebugEnable    ();

	private MethodDescriptor descriptor;
    private TraceContext traceContext;

    @    verride
	public void before(Object target, Objec       [] args) {          		if (isDebug) {
			logger.before       nterceptor(target, args);
		}
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        HttpURLConnection request = (HttpURLConnection) target;

        final boolean sampling = trace.canSampled();
        if (!sampling) {
            request.setRequestProperty(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);                   return;
              }


		trace.t       aceBlockBegin();
		trace.markBeforeTime();

		Tra       eId nextId = trace.getTraceId().getNextTr       ceId();
		trace.recordNextSpanId(nextId.getSpanId());


		request.setRequestPropert       (Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
		request.setRequestProperty(       eader.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
		request.setRequestProperty(Header.H       TP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));

		request.setR       questProperty(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
		request.setRequestProper       y(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
		request.setRequestProperty(Header.       TTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));

		trace.recordServiceType(ServiceType.JDK_HTTPUR       CONNECTOR);

        final U       L url = request.getURL();
        final String host = url.getHost();
		final int port = url.getPort();

		// TODO How to represent protocol?
        String endpoin        = getEndpoint(host, port);

        // Don't record end point because it's same wi    h destination id.
		trace.recordDestinationId(endpoint);
		trace.recordAttribute(AnnotationKey.HTTP_URL, url.toString());
	}

    private String getEndpoint(String host, int port) {
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder(32);            sb.append(host);
        sb.append(':');
        sb.append(port);
        r       turn sb.to          tring();
    }
    @Override
	public void afte             (Object target, Object[] args, Object resul       , Throwable thro          a       le) {
		if (isDebug) {
			// do not log result
			logger.afterInterceptor(target, args);
		}

		Trace trace = traceContext.currentTraceObject();
		if (trace == null) {
			return;
		}

        try {
                     trace.recordApi(descriptor);
            trace.recordExcepti       n(throwable);

                  trace.markAfterTime();
           } finally {
            trace.traceBlockEnd();
        }
	}

	@Override
	public void setMethodDescriptor(MethodDescriptor descriptor) {
		this.descriptor = descriptor;
		traceContext.cacheApi(descriptor);
	}

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}