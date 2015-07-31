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

package com.navercorp.pinpoint.bootstrap.interceptor;

/**
 * C-style API (doesn't return an object) in order to reduce the number of object instantiating 
 * @author emeroad
 */
public interface ParameterExtractor {
    public static final Object NULL = new Object();

    public static final int NOT_FOUND = -1;

    int getIndex();

    Object extractObject(Object[] parameterList);
}
