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
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DefaultUriStatRecorder<T> implements UriStatRecorder<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String NOT_FOUNDED = "/NOT_FOUND_URI";
    private final String[] DEFAULT_OFTEN_USED_URL = {"/", "/index.html", "/main"};
    private final List<String> defaultResourcePostfixes;

    private final UriExtractor<T> uriExtractor;
    private final UriStatStorage uriStatStorage;


    public DefaultUriStatRecorder(UriExtractor<T> uriExtractor, UriStatStorage uriStatStorage, String oftenUsedResources) {
        this.uriExtractor = Objects.requireNonNull(uriExtractor, "uriExtractor");
        this.uriStatStorage = Objects.requireNonNull(uriStatStorage, "uriStatStorage");
        String resourcesPostfixes = Objects.requireNonNull(oftenUsedResources, "oftenUsedResources");
        this.defaultResourcePostfixes = StringUtils.tokenizeToStringList(resourcesPostfixes, ",");
    }

    @Override
    public void record(String uriTemplate, T request, String rawUri, boolean status, long startTime, long endTime) {
        String uri;

        String userAttributeUri = uriExtractor.getUri(request, rawUri);
        if (userAttributeUri != null) {
            uri = userAttributeUri;
        } else if (uriTemplate != null) {
            uri = uriTemplate;
        } else {
            uri = checkOftenUsed(rawUri);
        }

        if (uri == null) {
            uri = NOT_FOUNDED;
            logger.debug("can not extract uri. request:{}, rawUri:{}", request, rawUri);
        }
        uriStatStorage.store(uri, status, startTime, endTime);
    }

    private String checkOftenUsed(String rawUri) {
        for (String oftenUsedUrl : DEFAULT_OFTEN_USED_URL) {
            if (oftenUsedUrl.equals(rawUri)) {
                return oftenUsedUrl;
            }
        }
        for (String oftenUsedResources : defaultResourcePostfixes) {
            if (rawUri.endsWith(oftenUsedResources)) {
                return "*" + oftenUsedResources;
            }
        }
        return null;
    }

}
