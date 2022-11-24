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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorder;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DefaultUriStatRecorder<T> implements UriStatRecorder<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final UriExtractor<T> uriExtractor;
    private final UriStatStorage uriStatStorage;

    private final String NOT_FOUNDED = "/NOT_FOUND_URI";

    public DefaultUriStatRecorder(UriExtractor<T> uriExtractor, UriStatStorage uriStatStorage) {
        this.uriExtractor = Objects.requireNonNull(uriExtractor, "uriExtractor");
        this.uriStatStorage = Objects.requireNonNull(uriStatStorage, "uriStatStorage");
    }

    @Override
    public void record(Trace trace, T request, String rawUri, boolean status, long startTime, long endTime) {
        String uri;

        String userAttributeUri = uriExtractor.getUri(request, rawUri);
        String interceptedUri = trace.getUriTemplate();
        if (userAttributeUri != null) {
            uri = userAttributeUri;
        } else if (interceptedUri != null) {
            uri = interceptedUri;
        } else {
            uri = NOT_FOUNDED;
            logger.warn("can not extract uri. request:{}, rawUri:{}", request, rawUri);
        }

        uriStatStorage.store(uri, status, startTime, endTime);
    }

}
