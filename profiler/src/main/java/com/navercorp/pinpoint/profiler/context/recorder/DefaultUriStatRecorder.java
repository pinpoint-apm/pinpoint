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

import com.navercorp.pinpoint.bootstrap.plugin.http.URITemplate;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorder;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DefaultUriStatRecorder<T> implements UriStatRecorder<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String[] DEFAULT_OFTEN_USED_URL = {"/", "/index.html", "/main"};
    private final List<String> defaultResourcePostfixes;

    private final UriExtractor<T> uriExtractor;


    public DefaultUriStatRecorder(UriExtractor<T> uriExtractor, String oftenUsedResources) {
        this.uriExtractor = Objects.requireNonNull(uriExtractor, "uriExtractor");
        String resourcesPostfixes = Objects.requireNonNull(oftenUsedResources, "oftenUsedResources");
        this.defaultResourcePostfixes = StringUtils.tokenizeToStringList(resourcesPostfixes, ",");
    }

    @Override
    public String record(String uriTemplate, T request, String rawUri) {

        String userAttributeUri = uriExtractor.getUri(request, rawUri);
        if (userAttributeUri != null) {
            return userAttributeUri;
        } else if (uriTemplate != null) {
            return uriTemplate;
        }

        String uri = checkOftenUsed(rawUri);
        if (uri == null) {
            uri = URITemplate.NOT_FOUNDED;
        }
        return uri;
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
