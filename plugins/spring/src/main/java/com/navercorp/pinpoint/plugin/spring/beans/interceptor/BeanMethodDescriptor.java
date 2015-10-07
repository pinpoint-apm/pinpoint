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
package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;

/**
 * @author Jongho Moon
 *
 */
public class BeanMethodDescriptor implements MethodDescriptor {
    private final String fullName;
    private int apiId;
    
    public BeanMethodDescriptor(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getMethodName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClassName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getParameterTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getParameterVariableName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParameterDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLineNumber() {
        return -1;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override
    public int getApiId() {
        return apiId;
    }

    @Override
    public String getApiDescriptor() {
        return fullName;
    }

    @Override
    public int getType() {
        return 0;
    }

}
