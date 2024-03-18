/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.method;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */

public class DefaultPredefinedMethodDescriptorRegistry implements PredefinedMethodDescriptorRegistry {

    private final ApiMetaDataService apiMetaDataService;

    private final MethodDescriptor asyncMethodDescriptor = new AsyncMethodDescriptor();

    @Inject
    public DefaultPredefinedMethodDescriptorRegistry(ApiMetaDataService apiMetaDataService) {
        this.apiMetaDataService = Objects.requireNonNull(apiMetaDataService, "apiMetaDataService");

        registryMethodDescriptor();
    }

    private void registryMethodDescriptor() {

        this.apiMetaDataService.cacheApi(asyncMethodDescriptor);
    }


    @Override
    public MethodDescriptor getAsyncMethodDescriptor() {
        return asyncMethodDescriptor;
    }
}
