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

package com.navercorp.pinpoint.profiler.context.recorder;

import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorder;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.UriStatInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class DefaultUriStatRecorder<T> implements UriStatRecorder<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UriExtractor<T> uriExtractor;
    private final UriStatStorage uriStatStorage;

    public DefaultUriStatRecorder(UriExtractor<T> uriExtractor, UriStatStorage uriStatStorage) {
        this.uriExtractor = Assert.requireNonNull(uriExtractor, "uriExtractor");
        this.uriStatStorage = Assert.requireNonNull(uriStatStorage, "uriStatStorage");
    }

    @Override
    public void record(T request, String rawUri, boolean status, long startTime, long endTime) {
        String uri = uriExtractor.getUri(request, rawUri);
        if (uri == null) {
            logger.warn("can not extract uri. request:{}, rawUri:{}", request, rawUri);
            return;
        }

        uriStatStorage.store(uri, status, endTime - startTime);
    }

}
