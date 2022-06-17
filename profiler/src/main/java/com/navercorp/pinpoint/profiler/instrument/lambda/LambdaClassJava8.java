/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.lambda;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaClassJava8 implements LambdaClass {

    public static final String DELEGATE_CLASS = "com/navercorp/pinpoint/bootstrap/lambda/UnsafeDelegatorJava8";

    @Override
    public String getUnsafeClass() {
        return "sun/misc/Unsafe";
    }

    @Override
    public String getUnsafeMethod() {
        return "defineAnonymousClass";
    }

    @Override
    public String getDelegateClass() {
        return DELEGATE_CLASS;
    }

    @Override
    public String getDelegateMethod() {
        return "defineAnonymousClass";
    }
}
