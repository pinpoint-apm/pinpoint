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

package com.navercorp.pinpoint.bootstrap.plugin.uri;

import com.navercorp.pinpoint.common.trace.UriExtractorType;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.net.URI;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class UriExtractorChain<T> implements UriExtractor<T> {

    private List<UriExtractor<T>> uriExtractorList;

    public UriExtractorChain(List<UriExtractor<T>> uriExtractorList) {
        if (CollectionUtils.isEmpty(uriExtractorList)) {
            throw new IllegalArgumentException("uriExtractorList may not be empty");
        }
        this.uriExtractorList = uriExtractorList;
    }

    @Override
    public UriExtractorType getExtractorType() {
        return uriExtractorList.get(0).getExtractorType();
    }

    @Override
    public String getUri(T target, String rawUrl) {
        for (UriExtractor<T> uriExtractor : uriExtractorList) {
            final String uri = uriExtractor.getUri(target, rawUrl);
            if (uri != null) {
                return uri;
            }
        }

        for (String oftenUsedUrl : DEFAULT_OFTEN_USED_URL) {
            if (oftenUsedUrl.equals(rawUrl)) {
                return oftenUsedUrl;
            }
        }

        return NOT_FOUNDED;
    }

}
