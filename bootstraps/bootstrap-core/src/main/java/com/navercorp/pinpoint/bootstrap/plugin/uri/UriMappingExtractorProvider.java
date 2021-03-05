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

package com.navercorp.pinpoint.bootstrap.plugin.uri;

import com.navercorp.pinpoint.common.trace.UriExtractorType;
import com.navercorp.pinpoint.common.util.ArrayUtils;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class UriMappingExtractorProvider implements UriExtractorProvider {

    private UriExtractorType uriExtractorType;
    private String[] mappingKeyCandidates;

    public UriMappingExtractorProvider(UriExtractorType uriExtractorType, String[] mappingKeyCandidates) {
        this.uriExtractorType = uriExtractorType;

        if (ArrayUtils.isEmpty(mappingKeyCandidates)) {
            throw new IllegalArgumentException("mappingKeyCandidates may not be empty");
        }
        this.mappingKeyCandidates = mappingKeyCandidates;
    }

    @Override
    public UriExtractorType getUriExtractorType() {
        return uriExtractorType;
    }

    public String[] getMappingKeyCandidates() {
        return mappingKeyCandidates;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UriMappingExtractorProvider{");
        sb.append("uriExtractorType=").append(uriExtractorType);
        sb.append(", mappingKeyCandidates=").append(Arrays.toString(mappingKeyCandidates));
        sb.append('}');
        return sb.toString();
    }
}
