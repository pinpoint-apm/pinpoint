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

import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.profiler.plugin.DefaultInterceptorStack;


public class DefaultScopeTest {
    @Test
    public void test0() {
        InterceptorGroupInvocation transaction = new DefaultInterceptorStack("test");
        
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
        InterceptorGroupInvocation transaction = new DefaultInterceptorStack("test");
        
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
        InterceptorGroupInvocation transaction = new DefaultInterceptorStack("test");
        
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
        InterceptorGroupInvocation transaction = new DefaultInterceptorStack("test");

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
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");

        trnasaction.tryEnter(ExecutionPolicy.ALWAYS);
        trnasaction.setAttachment(oldAttachment);
        assertSame(oldAttachment, trnasaction.getAttachment());
        assertSame(oldAttachment, trnasaction.setAttachment(newAttachment));
        assertSame(newAttachment, trnasaction.getAttachment());
        assertSame(newAttachment, trnasaction.removeAttachment());
        assertNull(trnasaction.getAttachment());
        trnasaction.canLeave(ExecutionPolicy.ALWAYS);
        trnasaction.leave(ExecutionPolicy.ALWAYS);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail() {
        InterceptorGroupInvocation transaction = new DefaultInterceptorStack("test");
        transaction.setAttachment("attachment");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testSetAttachmentFail2() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");

        trnasaction.tryEnter(ExecutionPolicy.ALWAYS);
        trnasaction.canLeave(ExecutionPolicy.ALWAYS);
        trnasaction.leave(ExecutionPolicy.ALWAYS);

        trnasaction.setAttachment("attachment");
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetAttachmentFail() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");
        trnasaction.getAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testGetAttachmentFail2() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");

        trnasaction.tryEnter(ExecutionPolicy.ALWAYS);
        trnasaction.canLeave(ExecutionPolicy.ALWAYS);
        trnasaction.leave(ExecutionPolicy.ALWAYS);

        trnasaction.getAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testRemoveAttachmentFail() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");
        trnasaction.removeAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testRemoveAttachmentFail2() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");

        trnasaction.tryEnter(ExecutionPolicy.ALWAYS);
        trnasaction.canLeave(ExecutionPolicy.ALWAYS);
        trnasaction.leave(ExecutionPolicy.ALWAYS);

        trnasaction.removeAttachment();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");
        trnasaction.leave(ExecutionPolicy.ALWAYS);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore2() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");
        trnasaction.leave(ExecutionPolicy.BOUNDARY);
    }
    
    @Test(expected=IllegalStateException.class)
    public void testAfterWithoutBefore3() {
        InterceptorGroupInvocation trnasaction = new DefaultInterceptorStack("test");
        trnasaction.leave(ExecutionPolicy.INTERNAL);
    }
}