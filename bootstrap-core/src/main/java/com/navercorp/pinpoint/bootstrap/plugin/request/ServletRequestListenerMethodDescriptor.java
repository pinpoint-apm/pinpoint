/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;

/**
 * @author jaehong.kim
 */
public class ServletRequestListenerMethodDescriptor implements MethodDescriptor {

    private int apiId;

    @Override
    public String getMethodName() {
        return "request";
    }

    @Override
    public String getClassName() {
        return "ServletRequestListener";
    }

    @Override
    public String[] getParameterTypes() {
        return new String[] {"javax.servlet.ServletRequestEvent"};
    }

    @Override
    public String[] getParameterVariableName() {
        return new String[] {"javax.servlet.ServletRequestEvent"};
    }

    @Override
    public String getParameterDescriptor() {
        return "javax.servlet.ServletRequestEvent";
    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    @Override
    public String getFullName() {
        return "javax.servlet.ServletRequestListener.request";
    }

    @Override
    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override
    public int getApiId() {
        return this.apiId;
    }

    @Override
    public String getApiDescriptor() {
        return "ServletRequestListener.request()";
    }

    @Override
    public int getType() {
        return 0;
    }
}
