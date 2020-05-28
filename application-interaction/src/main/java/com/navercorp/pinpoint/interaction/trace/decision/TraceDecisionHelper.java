/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.interaction.trace.decision;

import com.navercorp.pinpoint.interaction.util.ClassLoaderUtils;
import com.navercorp.pinpoint.interaction.util.InstanceUtils;

import java.lang.reflect.Method;

/**
 * @author yjqg6666
 */
@SuppressWarnings("unused")
public class TraceDecisionHelper {

    private static final String HOLDER_CLZ_NAME = "com.navercorp.pinpoint.interaction.trace.decision.TraceDecisionMakerHolder";

    private TraceDecisionHelper() {
    }

    /**
     * @param request servlet request
     * @return null whether tracing depend on sampler, true=force trace, false=disable trace
     */
    public static TraceDecisionEnum shouldTrace(Object request) {
        try {
            if (!InstanceUtils.isServletRequest(request)) {
                return TraceDecisionEnum.RateTrace;
            }
            Class<?> holderClass = ClassLoaderUtils.loadClassFromAppObject(request, HOLDER_CLZ_NAME);
            if (holderClass == null) {
                return TraceDecisionEnum.RateTrace;
            }
            Method getTraceDecisionMakerMethod = holderClass.getDeclaredMethod("getTraceDecisionMaker");
            Object traceDecisionMaker = getTraceDecisionMakerMethod.invoke(holderClass);
            if (traceDecisionMaker instanceof TraceDecisionMaker) {
                return ((TraceDecisionMaker) traceDecisionMaker).shouldTrace(request);
            }
        } catch (Throwable t) {
            //do nothing even no logging for no introduced dependency
            //t.printStackTrace();
        }
        return TraceDecisionEnum.RateTrace;
    }


}
