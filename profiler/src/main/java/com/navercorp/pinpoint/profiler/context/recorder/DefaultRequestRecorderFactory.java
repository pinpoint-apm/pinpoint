/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.recorder;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.DisableRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.DefaultProxyRequestRecorder;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParser;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParserLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class DefaultRequestRecorderFactory<T> implements RequestRecorderFactory<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProxyRequestParserLoaderService proxyRequestParserLoaderService;
    private final boolean enable;

    @Inject
    public DefaultRequestRecorderFactory(final ProxyRequestParserLoaderService proxyRequestParserLoaderService, ProfilerConfig profilerConfig) {
        this.proxyRequestParserLoaderService = proxyRequestParserLoaderService;
        this.enable = profilerConfig.isProxyHttpHeaderEnable();
    }

    @Override
    public ProxyRequestRecorder<T> getProxyRequestRecorder(final RequestAdaptor<T> requestAdaptor) {
        if (!enable) {
            if (logger.isDebugEnabled()) {
                logger.debug("Disable record proxy http header.");
            }
            return new DisableRequestRecorder<T>();
        }
        final List<ProxyRequestParser> proxyRequestParserList = this.proxyRequestParserLoaderService.getProxyRequestParserList();
        return new DefaultProxyRequestRecorder<T>(proxyRequestParserList, requestAdaptor);
    }
}