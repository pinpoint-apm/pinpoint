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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.instrument.AttachmentScope;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.http.HttpCallContext;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * MethodInfo interceptor
 * <p/>
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public <T> T execute(
 *            final HttpHost target,
 *            final HttpRequest request,
 *            final ResponseHandler<? extends T> responseHandler,
 *            final HttpContext context)
 *            throws IOException, ClientProtocolException {
 * </pre>
 * @author emeroad
 */
public class HttpRequestExecuteInterceptor extends AbstractHttpRequestExecute implements TargetClassLoader {

    private static final int HTTP_HOST_INDEX = 0;
    private static final int HTTP_REQUEST_INDEX = 1;

    private boolean isHasCallbackParam;
    private AttachmentScope<HttpCallContext> scope;
    
    public HttpRequestExecuteInterceptor(boolean isHasCallbackParam, Scope scope) {
        super(HttpRequestExecuteInterceptor.class);
        this.isHasCallbackParam = isHasCallbackParam;
        this.scope = (AttachmentScope<HttpCallContext>)scope;
    }
    
//    public HttpRequestExecuteInterceptor() {
//        super(HttpRequestExecuteInterceptor.class);
//    }

    @Override
    public void before(Object target, Object[] args) {
        if (!isPassibleBeforeProcess()) {
            return;
        }
        
        super.before(target, args);
    }
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!isPassibleAfterProcess()) {
            addStatusCode(result);
            scope.pop();
            return;
        }
        
        super.after(target, args, result, throwable);
        scope.pop();
    };
    
    private boolean isPassibleBeforeProcess() {
        if (scope.push() == Scope.ZERO) {
            return true;
        }
        
        return false;
    }
    
    private boolean isPassibleAfterProcess() {
        final int depth = scope.depth();
        
        if (depth - 1 == Scope.ZERO) {
            return true;
        }
        
        return false;
    }
    
    private void addStatusCode(Object result) {
        if(!needGetStatusCode()) {
            return;
        }
        
        if (result instanceof HttpResponse) {
            HttpResponse response = (HttpResponse)result;
            
            if (response.getStatusLine() != null) {
                HttpCallContext context = new HttpCallContext();
                context.setStatusCode(response.getStatusLine().getStatusCode());
                scope.setAttachment(context);
            }
        }
    }
    
    private boolean needGetStatusCode() {
        if (isHasCallbackParam) {
            return false;
        }
        
        final Trace trace = traceContext.currentRawTraceObject();
        
        if (trace == null || trace.getServiceType() != ServiceType.HTTP_CLIENT) {
            return false;
        }

        if(scope.getAttachment() != null) {
            return false;
        }

        return true;
    }
    
    @Override
    protected NameIntValuePair<String> getHost(Object[] args) {
        final Object arg = args[HTTP_HOST_INDEX];
        if (arg instanceof HttpHost) {
            final HttpHost httpHost = (HttpHost) arg;
            return new NameIntValuePair<String>(httpHost.getHostName(), httpHost.getPort());
        }
        return null;
    }

    @Override
    protected HttpRequest getHttpRequest(Object[] args) {
        final Object arg = args[HTTP_REQUEST_INDEX];
        if (arg instanceof HttpRequest) {
            return (HttpRequest) arg;
        }
        return null;
    }
    
    @Override
    Integer getStatusCode(Object result) {
        if (result instanceof HttpResponse) {
            HttpResponse response = (HttpResponse)result;
            
            if (response.getStatusLine() != null) {
                return response.getStatusLine().getStatusCode(); 
            }
        }
        
        if (scope.getAttachment() != null && scope.getAttachment() instanceof HttpCallContext) {
            return ((HttpCallContext)scope.getAttachment()).getStatusCode();
        }
        
        return null;
    }
}