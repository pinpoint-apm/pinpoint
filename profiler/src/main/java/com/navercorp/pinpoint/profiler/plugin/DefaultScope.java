/**
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
package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.instrument.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPoint;

/**
 * @author Jongho Moon
 *
 */
public class DefaultScope implements Scope {
    private final String name;
    private Object attachment = null;
    private int depth = 0;
    
    public DefaultScope(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean tryBefore(ExecutionPoint point) {
        switch (point) {
        case BOUNDARY:
            return ++depth == 1;
        case INTERIOR:
            return depth > 0;
        case ALWAYS:
            depth++;
            return true;
        }
        
        throw new IllegalArgumentException("Unexpected: " + point);
    }
    
    private void decreaseDepth() {
        if (depth == 0) {
            throw new IllegalStateException();
        }
        
        depth--;
        
        if (depth == 0) {
            attachment = null;
        }
    }

    @Override
    public boolean tryAfter(ExecutionPoint point) {
        switch (point) {
        case BOUNDARY:
            decreaseDepth();
            return depth == 0;
        case INTERIOR:
            return depth > 0;
        case ALWAYS:
            decreaseDepth();
            return true;
        }
        
        throw new IllegalArgumentException("Unexpected: " + point);
    }

    @Override
    public boolean isIn() {
        return depth > 0;
    }

    @Override
    public Object setAttachment(Object attachment) {
        if (!isIn()) {
            throw new IllegalStateException();
        }
        
        Object old = this.attachment;
        this.attachment = attachment;
        return old;
    }
    
    @Override
    public Object getOrCreateAttachment(AttachmentFactory factory) {
        if (!isIn()) {
            throw new IllegalStateException();
        }
        
        if (attachment == null) {
            attachment = factory.createAttachment();
        }
        
        return attachment;
    }

    @Override
    public Object getAttachment() {
        if (!isIn()) {
            throw new IllegalStateException();
        }
        
        return attachment;
    }

    @Override
    public Object removeAttachment() {
        if (!isIn()) {
            throw new IllegalStateException();
        }
        
        Object old = this.attachment;
        this.attachment = null;
        return old;
    }

    @Override
    public String toString() {
        return "Group(" + name + ")[depth=" + depth +"]";
    }
    
    
}
