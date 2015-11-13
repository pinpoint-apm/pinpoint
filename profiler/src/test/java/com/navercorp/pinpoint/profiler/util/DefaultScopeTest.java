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

import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.profiler.interceptor.scope.DefaultInterceptorScopeInvocation;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;


public class DefaultScopeTest {
    @Test
    public void test0() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        
        assertFalse(transaction.isActive());
        
        assertFalse(transaction.tryEnter(ExecutionPolicy.INTERNAL));
        assertFalse(transaction.isActive());
        
        assertTrue(transaction.tryEnter(ExecutionPolicy.BOUNDARY));
        assertTrue(transaction.isActive());
        
        assertTrue(transaction.tryEnter(ExecutionPolicy.INTERNAL));
        assertTrue(transaction.isActive());
        
        assertFalse(transaction.tryEnter(ExecutionPolicy.BOUNDARY));
        assertTrue(transaction.isActive());
        
        assertFalse(transaction.canLeave(ExecutionPolicy.BOUNDARY));
        assertTrue(transaction.isActive());
        
        assertTrue(transaction.canLeave(ExecutionPolicy.INTERNAL));
        transaction.leave(ExecutionPolicy.INTERNAL);
        assertTrue(transaction.isActive());
        
        assertTrue(transaction.canLeave(ExecutionPolicy.BOUNDARY));
        assertTrue(transaction.isActive());
        transaction.leave(ExecutionPolicy.BOUNDARY);
        assertFalse(transaction.isActive());
        
        assertFalse(transaction.canLeave(ExecutionPolicy.INTERNAL));
        assertFalse(transaction.isActive());
    }
    
    @Test
    public void test1() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        
        assertFalse(transaction.isActive());
        
        assertTrue(transaction.tryEnter(ExecutionPolicy.ALWAYS));
        assertTrue(transaction.isActive());
        
        assertTrue(transaction.tryEnter(ExecutionPolicy.ALWAYS));
        assertTrue(transaction.isActive());
        
        assertTrue(transaction.canLeave(ExecutionPolicy.ALWAYS));
        assertTrue(transaction.isActive());
        transaction.leave(ExecutionPolicy.ALWAYS);
        assertTrue(transaction.isActive());
        
        assertTrue(transaction.canLeave(ExecutionPolicy.ALWAYS));
        assertTrue(transaction.isActive());
        transaction.leave(ExecutionPolicy.ALWAYS);
        assertFalse(transaction.isActive());
    }

    @Test
    public void testAttachment() {
        String attachment = "context";
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        
        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        assertNull(transaction.getAttachment());
        
        transaction.setAttachment(attachment);
        assertSame(transaction.getAttachment(), attachment);

        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        assertSame(transaction.getAttachment(), attachment);

        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);
        assertSame(transaction.getAttachment(), attachment);
        
        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);
    }

    @Test
    public void testAttachment2() {
        String attachment = "context";
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");

        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        assertNull(transaction.getAttachment());
        transaction.setAttachment(attachment);
        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);

        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        assertNull(transaction.getAttachment());
        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);
    }
    
    @Test
    public void testAttachment3() {
        String oldAttachment = "context";
        String newAttachment = "newnew";
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");

        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        transaction.setAttachment(oldAttachment);
        assertSame(oldAttachment, transaction.getAttachment());
        assertSame(oldAttachment, transaction.setAttachment(newAttachment));
        assertSame(newAttachment, transaction.getAttachment());
        assertSame(newAttachment, transaction.removeAttachment());
        assertNull(transaction.getAttachment());
        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        transaction.setAttachment("attachment");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail2() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");

        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);

        transaction.setAttachment("attachment");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetAttachmentFail() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        transaction.getAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetAttachmentFail2() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");

        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);

        transaction.getAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testRemoveAttachmentFail() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        transaction.removeAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testRemoveAttachmentFail2() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");

        transaction.tryEnter(ExecutionPolicy.ALWAYS);
        transaction.canLeave(ExecutionPolicy.ALWAYS);
        transaction.leave(ExecutionPolicy.ALWAYS);

        transaction.removeAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        transaction.leave(ExecutionPolicy.ALWAYS);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore2() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        transaction.leave(ExecutionPolicy.BOUNDARY);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore3() {
        InterceptorScopeInvocation transaction = new DefaultInterceptorScopeInvocation("test");
        transaction.leave(ExecutionPolicy.INTERNAL);
    }
}