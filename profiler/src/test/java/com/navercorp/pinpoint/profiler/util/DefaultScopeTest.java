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

package com.navercorp.pinpoint.profiler.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.plugin.interceptor.ExecutionPoint;
import com.navercorp.pinpoint.profiler.plugin.DefaultScope;


public class DefaultScopeTest {
    @Test
    public void test0() {
        Scope scope = new DefaultScope("test");
        
        assertFalse(scope.isIn());
        
        assertFalse(scope.tryBefore(ExecutionPoint.INTERIOR));
        assertFalse(scope.isIn());
        
        assertTrue(scope.tryBefore(ExecutionPoint.BOUNDARY));
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryBefore(ExecutionPoint.INTERIOR));
        assertTrue(scope.isIn());
        
        assertFalse(scope.tryBefore(ExecutionPoint.BOUNDARY));
        assertTrue(scope.isIn());
        
        assertFalse(scope.tryAfter(ExecutionPoint.BOUNDARY));
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryAfter(ExecutionPoint.INTERIOR));
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryAfter(ExecutionPoint.BOUNDARY));
        assertFalse(scope.isIn());
        
        assertFalse(scope.tryAfter(ExecutionPoint.INTERIOR));
        assertFalse(scope.isIn());
    }
    
    @Test
    public void test1() {
        Scope scope = new DefaultScope("test");
        
        assertFalse(scope.isIn());
        
        assertTrue(scope.tryBefore(ExecutionPoint.ALWAYS));
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryBefore(ExecutionPoint.ALWAYS));
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryAfter(ExecutionPoint.ALWAYS));
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryAfter(ExecutionPoint.ALWAYS));
        assertFalse(scope.isIn());
    }

    @Test
    public void testAttachment() {
        String attachment = "context";
        Scope scope = new DefaultScope("test");
        
        scope.tryBefore(ExecutionPoint.BOUNDARY);
        assertNull(scope.getAttachment());
        
        scope.setAttachment(attachment);
        assertSame(scope.getAttachment(), attachment);

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        assertSame(scope.getAttachment(), attachment);

        scope.tryAfter(ExecutionPoint.BOUNDARY);
        assertSame(scope.getAttachment(), attachment);
        
        scope.tryAfter(ExecutionPoint.BOUNDARY);
    }

    @Test
    public void testAttachment2() {
        String attachment = "context";
        Scope scope = new DefaultScope("test");

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        assertNull(scope.getAttachment());
        scope.setAttachment(attachment);
        scope.tryAfter(ExecutionPoint.BOUNDARY);

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        assertNull(scope.getAttachment());
        scope.tryAfter(ExecutionPoint.BOUNDARY);
    }
    
    @Test
    public void testAttachment3() {
        String oldAttachment = "context";
        String newAttachment = "newnew";
        Scope scope = new DefaultScope("test");

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        scope.setAttachment(oldAttachment);
        assertSame(oldAttachment, scope.getAttachment());
        assertSame(oldAttachment, scope.setAttachment(newAttachment));
        assertSame(newAttachment, scope.getAttachment());
        assertSame(newAttachment, scope.removeAttachment());
        assertNull(scope.getAttachment());
        scope.tryAfter(ExecutionPoint.BOUNDARY);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail() {
        Scope scope = new DefaultScope("test");
        scope.setAttachment("attachment");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail2() {
        Scope scope = new DefaultScope("test");

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        scope.tryAfter(ExecutionPoint.BOUNDARY);

        scope.setAttachment("attachment");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetAttachmentFail() {
        Scope scope = new DefaultScope("test");
        scope.getAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetAttachmentFail2() {
        Scope scope = new DefaultScope("test");

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        scope.tryAfter(ExecutionPoint.BOUNDARY);

        scope.getAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testRemoveAttachmentFail() {
        Scope scope = new DefaultScope("test");
        scope.removeAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testRemoveAttachmentFail2() {
        Scope scope = new DefaultScope("test");

        scope.tryBefore(ExecutionPoint.BOUNDARY);
        scope.tryAfter(ExecutionPoint.BOUNDARY);

        scope.removeAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore() {
        Scope scope = new DefaultScope("test");
        scope.tryAfter(ExecutionPoint.ALWAYS);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore2() {
        Scope scope = new DefaultScope("test");
        scope.tryAfter(ExecutionPoint.BOUNDARY);
    }
    
    @Test
    public void testAfterWithoutBefore3() {
        Scope scope = new DefaultScope("test");
        assertFalse(scope.tryAfter(ExecutionPoint.INTERIOR));
    }
}