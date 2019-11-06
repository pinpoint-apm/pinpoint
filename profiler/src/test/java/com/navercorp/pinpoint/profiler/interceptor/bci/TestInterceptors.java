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
package com.navercorp.pinpoint.profiler.interceptor.bci;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

/**
 * @author Jongho Moon
 *
 */
public class TestInterceptors {

    private static final List<Interceptor> interceptors = Collections.synchronizedList(new ArrayList<Interceptor>());

    public static void clear() {
        interceptors.clear();
    }

    public static void add(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public static Interceptor get(int index) {
        return interceptors.get(index);
    }
}
