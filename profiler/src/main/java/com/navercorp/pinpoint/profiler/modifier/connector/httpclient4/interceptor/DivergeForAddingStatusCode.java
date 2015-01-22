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

import org.apache.http.HttpResponse;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.instrument.AttachmentScope;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.http.HttpCallContext;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author minwoo.jung
 */
public abstract class DivergeForAddingStatusCode extends AbstractHttpRequestExecute {
    
    private boolean isHasCallbackParam;
    private AttachmentScope<HttpCallContext> scope;
    
    public DivergeForAddingStatusCode(Class<? extends AbstractHttpRequestExecute> childClazz, boolean isHasCallbackParam, Scope scope) {
        super(childClazz);
        this.isHasCallbackParam = isHasCallbackParam;
        this.scope = (AttachmentScope<HttpCallContext>)scope;
    }
    
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
