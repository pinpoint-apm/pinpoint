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
package com.navercorp.pinpoint.profiler.interceptor.scope;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;

/**
 * @author Jongho Moon
 *
 */
public class DefaultInterceptorScopeInvocation implements InterceptorScopeInvocation {
    private final String name;
    private Object attachment = null;
    
    private int depth = 0;
    private int skippedBoundary = 0;
    
    public DefaultInterceptorScopeInvocation(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean tryEnter(ExecutionPolicy point) {
        switch (point) {
        case ALWAYS:
            depth++;
            return true;
        case BOUNDARY:
            if (isActive()) {
                skippedBoundary++;
                return false;
            } else {
                depth++;
                return true;
            }
        case INTERNAL:
            if (isActive()) {
                depth++;
                return true;
            } else {
                return false;
            }
        default:
            throw new IllegalArgumentException("Unexpected: " + point);
        }
    }
    
    @Override
    public boolean canLeave(ExecutionPolicy point) {
        switch (point) {
        case ALWAYS:
            return true;
        case BOUNDARY:
            if (skippedBoundary == 0 && depth == 1) {
                return true;
            } else {
                skippedBoundary--;
                return false;
            }
        case INTERNAL:
            return depth > 1;
        default:
            throw new IllegalArgumentException("Unexpected: " + point);
        }
    }

    @Override
    public void leave(ExecutionPolicy point) {
        if (depth == 0) {
            throw new IllegalStateException();
        }

        switch (point) {
        case ALWAYS:
            break;
            
        case BOUNDARY:
            if (skippedBoundary != 0 || depth != 1) {
                throw new IllegalStateException("Cannot leave with BOUNDARY interceptor. depth: " + depth);
            }
            break;
            
        case INTERNAL:
            if (depth <= 1) {
                throw new IllegalStateException("Cannot leave with INTERNAL interceptor. depth: " + depth);
            }
            break;
            
        default:
            throw new IllegalArgumentException("Unexpected: " + point);
        }

        if (--depth == 0) {
            attachment = null;
        }
    }


    @Override
    public boolean isActive() {
        return depth > 0;
    }

    @Override
    public Object setAttachment(Object attachment) {
        if (!isActive()) {
            throw new IllegalStateException();
        }
        
        Object old = this.attachment;
        this.attachment = attachment;
        return old;
    }
    
    @Override
    public Object getOrCreateAttachment(AttachmentFactory factory) {
        if (!isActive()) {
            throw new IllegalStateException();
        }
        
        if (attachment == null) {
            attachment = factory.createAttachment();
        }
        
        return attachment;
    }

    @Override
    public Object getAttachment() {
        if (!isActive()) {
            throw new IllegalStateException();
        }
        
        return attachment;
    }

    @Override
    public Object removeAttachment() {
        if (!isActive()) {
            throw new IllegalStateException();
        }
        
        Object old = this.attachment;
        this.attachment = null;
        return old;
    }

    @Override
    public String toString() {
        return "InterceptorScopeInvocation(" + name + ")[depth=" + depth +"]";
    }
    
    
}
