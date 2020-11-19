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
import com.navercorp.pinpoint.common.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class UriExtractorProviderRegistry implements UriExtractorProviderLocator {

    private final List<UriExtractorProvider> uriExtractorProviderList;

    public UriExtractorProviderRegistry(List<UriExtractorProvider> uriExtractorProviderList) {
        this.uriExtractorProviderList = Assert.requireNonNull(uriExtractorProviderList, "uriExtractorProviderList");
    }

    @Override
    public <T extends UriExtractorProvider> List<T> get(Class<T> type, UriExtractorType uriExtractorType) {
        List<T> result = new ArrayList<T>();
        for (UriExtractorProvider uriExtractorProvider : uriExtractorProviderList) {
            if (!type.isInstance(uriExtractorProvider)) {
                continue;
            }
            if (uriExtractorProvider.getUriExtractorType() == uriExtractorType) {
                result.add((T) uriExtractorProvider);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UriExtractorProviderRegistry{");
        sb.append("uriExtractorProviderList=").append(uriExtractorProviderList);
        sb.append('}');
        return sb.toString();
    }
}
