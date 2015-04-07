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
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPoint;
import com.navercorp.pinpoint.profiler.plugin.DefaultScope;


public class DefaultScopeTest {
    @Test
    public void test0() {
        Scope scope = new DefaultScope("test");
        
        assertFalse(scope.isIn());
        
        assertFalse(scope.tryEnter(ExecutionPoint.INTERNAL));
        assertFalse(scope.isIn());
        
        assertTrue(scope.tryEnter(ExecutionPoint.BOUNDARY));
        assertTrue(scope.isIn());
        scope.entered(ExecutionPoint.BOUNDARY);
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryEnter(ExecutionPoint.INTERNAL));
        assertTrue(scope.isIn());
        scope.entered(ExecutionPoint.INTERNAL);
        assertTrue(scope.isIn());
        
        assertFalse(scope.tryEnter(ExecutionPoint.BOUNDARY));
        assertTrue(scope.isIn());
        
        assertFalse(scope.tryLeave(ExecutionPoint.BOUNDARY));
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryLeave(ExecutionPoint.INTERNAL));
        scope.leaved(ExecutionPoint.INTERNAL);
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryLeave(ExecutionPoint.BOUNDARY));
        assertTrue(scope.isIn());
        scope.leaved(ExecutionPoint.BOUNDARY);
        assertFalse(scope.isIn());
        
        assertFalse(scope.tryLeave(ExecutionPoint.INTERNAL));
        assertFalse(scope.isIn());
    }
    
    @Test
    public void test1() {
        Scope scope = new DefaultScope("test");
        
        assertFalse(scope.isIn());
        
        assertTrue(scope.tryEnter(ExecutionPoint.ALWAYS));
        assertTrue(scope.isIn());
        scope.entered(ExecutionPoint.ALWAYS);
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryEnter(ExecutionPoint.ALWAYS));
        assertTrue(scope.isIn());
        scope.entered(ExecutionPoint.ALWAYS);
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryLeave(ExecutionPoint.ALWAYS));
        assertTrue(scope.isIn());
        scope.leaved(ExecutionPoint.ALWAYS);
        assertTrue(scope.isIn());
        
        assertTrue(scope.tryLeave(ExecutionPoint.ALWAYS));
        assertTrue(scope.isIn());
        scope.leaved(ExecutionPoint.ALWAYS);
        assertFalse(scope.isIn());
    }

    @Test
    public void testAttachment() {
        String attachment = "context";
        Scope scope = new DefaultScope("test");
        
        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        assertNull(scope.getAttachment());
        
        scope.setAttachment(attachment);
        assertSame(scope.getAttachment(), attachment);

        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        assertSame(scope.getAttachment(), attachment);

        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);
        assertSame(scope.getAttachment(), attachment);
        
        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);
    }

    @Test
    public void testAttachment2() {
        String attachment = "context";
        Scope scope = new DefaultScope("test");

        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        assertNull(scope.getAttachment());
        scope.setAttachment(attachment);
        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);

        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        assertNull(scope.getAttachment());
        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);
    }
    
    @Test
    public void testAttachment3() {
        String oldAttachment = "context";
        String newAttachment = "newnew";
        Scope scope = new DefaultScope("test");

        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        scope.setAttachment(oldAttachment);
        assertSame(oldAttachment, scope.getAttachment());
        assertSame(oldAttachment, scope.setAttachment(newAttachment));
        assertSame(newAttachment, scope.getAttachment());
        assertSame(newAttachment, scope.removeAttachment());
        assertNull(scope.getAttachment());
        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail() {
        Scope scope = new DefaultScope("test");
        scope.setAttachment("attachment");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail2() {
        Scope scope = new DefaultScope("test");

        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);

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

        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);

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

        scope.tryEnter(ExecutionPoint.ALWAYS);
        scope.entered(ExecutionPoint.ALWAYS);
        scope.tryLeave(ExecutionPoint.ALWAYS);
        scope.leaved(ExecutionPoint.ALWAYS);

        scope.removeAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore() {
        Scope scope = new DefaultScope("test");
        scope.leaved(ExecutionPoint.ALWAYS);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore2() {
        Scope scope = new DefaultScope("test");
        scope.leaved(ExecutionPoint.BOUNDARY);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore3() {
        Scope scope = new DefaultScope("test");
        scope.leaved(ExecutionPoint.INTERNAL);
    }
}