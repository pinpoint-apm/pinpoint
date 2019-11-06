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

package com.navercorp.pinpoint.plugin.websphere;

import com.ibm.websphere.servlet.request.IRequest;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.DisableParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.HttpParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;

/**
 * @author jaehong.kim
 */
public class ParameterRecorderFactory {
    public static ParameterRecorder<IRequest> newParameterRecorderFactory(Filter<String> excludeProfileMethodFilter, boolean traceRequestParam) {
        if (!traceRequestParam) {
            return new DisableParameterRecorder<IRequest>();
        }
        ParameterExtractor<IRequest> parameterExtractor = new IRequestParameterExtractor(64, 512);
        ParameterExtractor<IRequest> methodFilterExtractor = new MethodFilterExtractor(excludeProfileMethodFilter, parameterExtractor);
        return new HttpParameterRecorder<IRequest>(methodFilterExtractor);
    }
}
