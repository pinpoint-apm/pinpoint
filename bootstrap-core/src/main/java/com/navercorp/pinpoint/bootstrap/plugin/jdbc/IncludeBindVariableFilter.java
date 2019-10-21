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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import java.lang.reflect.Method;

/**
 * @author emeroad
 */
public class IncludeBindVariableFilter implements BindVariableFilter {
    private final String[] includes;

    public IncludeBindVariableFilter(String[] includes) {
        if (includes == null) {
            throw new NullPointerException("includes");
        }
        this.includes = includes;
    }

    @Override
    public boolean filter(Method method) {
        if (method == null) {
            throw new NullPointerException("method");
        }
        for (String include: includes) {
            if (method.getName().equals(include)) {
                return true;
            }
        }
        return false;
    }
}
