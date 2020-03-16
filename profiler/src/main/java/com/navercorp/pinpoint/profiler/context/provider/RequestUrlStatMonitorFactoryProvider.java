/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlStatMonitorFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.monitor.DefaultRequestUrlStatMonitorFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Taejin Koo
 */
public class RequestUrlStatMonitorFactoryProvider implements Provider<RequestUrlStatMonitorFactory> {

    private final DataSender dataSender;

    @Inject
    public RequestUrlStatMonitorFactoryProvider(@StatDataSender DataSender dataSender) {
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender");
    }

    @Override
    public RequestUrlStatMonitorFactory get() {
        DefaultRequestUrlStatMonitorFactory requestStatMonitorFactory = new DefaultRequestUrlStatMonitorFactory(dataSender);
        return requestStatMonitorFactory;
    }

}
