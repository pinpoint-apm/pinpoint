/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.async.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.spring.async.SpringAsyncConfig;

import org.springframework.core.task.AsyncTaskExecutor;

import java.util.Set;

/**
 * @author Taejin Koo
 */
public class AsyncExecutionAspectSupportInterceptor extends AsyncTaskExecutorSubmitInterceptor {

    private final Set<String> asyncTaskExecutorClassNameList;

    public AsyncExecutionAspectSupportInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        SpringAsyncConfig springAsyncConfig = new SpringAsyncConfig(traceContext.getProfilerConfig());
        this.asyncTaskExecutorClassNameList = springAsyncConfig.getAsyncTaskExecutorClassNameList();
    }

    @Override
    protected boolean validate(final Object[] args) {
        if (ArrayUtils.getLength(args) != 3) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
            }
            return false;
        }

        if ((args[1] instanceof AsyncTaskExecutor)) {
            String name = args[1].getClass().getName();
            return asyncTaskExecutorClassNameList.contains(name);
        }

        return false;
    }

}
