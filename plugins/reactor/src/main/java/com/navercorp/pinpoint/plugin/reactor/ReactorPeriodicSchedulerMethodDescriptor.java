/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.MethodType;

/**
 * @author jaehong.kim
 */
public class ReactorPeriodicSchedulerMethodDescriptor implements MethodDescriptor {
    private int apiId = 0;
    private int type = MethodType.WEB_REQUEST;

    @Override
    public String getMethodName() {
        return null;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String[] getParameterTypes() {
        return new String[0];
    }

    @Override
    public String[] getParameterVariableName() {
        return new String[0];
    }

    @Override
    public String getParameterDescriptor() {
        return null;
    }

    @Override
    public int getLineNumber() {
        return -1;
    }

    @Override
    public String getFullName() {
        return "com.navercorp.pinpoint.plugin.reactor.ReactorPeriodicSchedulerMethodDescriptor.call()";
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
        return "Reactor Periodic Scheduler Task";
    }

    @Override
    public int getType() {
        return this.type;
    }
}
