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
package com.navercorp.pinpoint.plugin.jackson.filter;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;

/**
 * Filter accepting methods with specified name only.
 * 
 * @author Sungkook Kim
 */
public class MethodNameFilter implements MethodFilter {
    private final String methodName;

    public MethodNameFilter(final String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean filter(MethodInfo method) {
        return !methodName.equals(method.getName());
    }

}
