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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

public class TestInterceptors {
    
    private static class AbstractInterceptor implements AroundInterceptor {
        
        @Override
        public void before(Object target, Object[] args) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {
            // TODO Auto-generated method stub
            
        }
        
    }

    public static class TestInterceptor0 extends AbstractInterceptor {
        private final String field0;

        public TestInterceptor0() {
            field0 = null;
        }
        
        public TestInterceptor0(String field0) {
            this.field0 = field0;
        }

        public String getField0() {
            return field0;
        }
    }
    
    public static class TestInterceptor1 extends AbstractInterceptor {
        private final String field0;
        private final byte field1;
        private final short field2;
        private final float field3;
        
        public TestInterceptor1(String field0, byte field1, short field2, float field3) {
            this.field0 = field0;
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        public TestInterceptor1(byte field1, short field2, float field3, String field0) {
            this.field0 = field0;
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
        
        public TestInterceptor1(short field2, float field3, String field0, byte field1) {
            this.field0 = field0;
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
        
        public TestInterceptor1(float field3, short field2, byte field1, String field0) {
            this.field0 = field0;
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
        
        public String getField0() {
            return field0;
        }

        public byte getField1() {
            return field1;
        }

        public short getField2() {
            return field2;
        }

        public float getField3() {
            return field3;
        }
    }
    
    public static class TestInterceptor2 extends AbstractInterceptor {
        private final TraceContext context;
        private final MethodDescriptor descriptor;
        private final InstrumentClass targetClass;
        private final InstrumentMethod targetMethod;
        
        private final String field0;
        private final int field1;
        private final double field2;
        private final boolean field3;
        private final long field4;
        

        public TestInterceptor2(String field0, TraceContext context, int field1, double field2, MethodDescriptor descriptor, boolean field3, InstrumentClass targetClass, InstrumentMethod targetMethod, long field4) {
            this.context = context;
            this.descriptor = descriptor;
            this.targetClass = targetClass;
            this.targetMethod = targetMethod;
            this.field0 = field0;
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.field4 = field4;
        }

        public TestInterceptor2(String field0, int field1, double field2, TraceContext context, InstrumentClass targetClass, MethodDescriptor descriptor, InstrumentMethod targetMethod) {
            this.context = context;
            this.descriptor = descriptor;
            this.targetClass = targetClass;
            this.targetMethod = targetMethod;
            this.field0 = field0;
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = false;
            this.field4 = 0;
        }
        
        public TestInterceptor2(TraceContext context, MethodDescriptor descriptor, InstrumentClass targetClass, InstrumentMethod targetMethod, String field0, int field1) {
            this.context = context;
            this.descriptor = descriptor;
            this.targetClass = targetClass;
            this.targetMethod = targetMethod;
            this.field0 = field0;
            this.field1 = field1;
            this.field2 = 0;
            this.field3 = false;
            this.field4 = 0;
        }
        
        public TestInterceptor2(TraceContext context, InstrumentClass targetClass, MethodDescriptor descriptor, InstrumentMethod targetMethod) {
            this.context = context;
            this.descriptor = descriptor;
            this.targetClass = targetClass;
            this.targetMethod = targetMethod;
            this.field0 = null;
            this.field1 = 0;
            this.field2 = 0;
            this.field3 = false;
            this.field4 = 0;
        }
        
        public TestInterceptor2(MethodDescriptor descriptor, InstrumentClass targetClass, String field0) {
            this.context = null;
            this.descriptor = descriptor;
            this.targetClass = targetClass;
            this.targetMethod = null;
            this.field0 = field0;
            this.field1 = 0;
            this.field2 = 0;
            this.field3 = false;
            this.field4 = 0;
        }

        public TraceContext getContext() {
            return context;
        }

        public MethodDescriptor getDescriptor() {
            return descriptor;
        }

        public InstrumentMethod getTargetMethod() {
            return targetMethod;
        }
        
        public InstrumentClass getTargetClass() {
            return targetClass;
        }

        public String getField0() {
            return field0;
        }

        public int getField1() {
            return field1;
        }

        public double getField2() {
            return field2;
        }

        public boolean getField3() {
            return field3;
        }

        public long getField4() {
            return field4;
        }
    }
}
