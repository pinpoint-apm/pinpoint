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

package com.navercorp.pinpoint.plugin.process.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.process.ProcessPluginConstants;

import java.util.List;

public class ProcessExecuteInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private static final PidProvider pidProvider = getPidMethod();


    public ProcessExecuteInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }


    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        recorder.recordServiceType(ProcessPluginConstants.SERVICE_TYPE);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        Process process = (Process) result;
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);

        List<String> commands = ((ProcessBuilder) target).command();
        recorder.recordAttribute(ProcessPluginConstants.PROCESS_COMMAND, commands.toString());

        Long pid = pidProvider.getPid(process);
        if (pid != null) {
            recorder.recordAttribute(ProcessPluginConstants.PROCESS_ID, pid);
        }
    }

    private static PidProvider getPidMethod() {
        PidProviderFactory factory = new PidProviderFactory();
        return factory.newPidProvider();
    }

}
