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

import java.net.URI;

/**
 * Class that extract URI from Request
 * (Ex : ServletRequestAttributesMappingExtractor is that extract URI from SevlerRequest's attributes)
 *
 * @author Taejin Koo
 */
public interface UriExtractor<T> {

    //    "javax.servlet.ServletRequest", "javax.servlet.ServletResponse"
    String NOT_FOUNDED = "/NOT_FOUND_URI";

    String[] DEFAULT_OFTEN_USED_URL = {"/", "/index.html"};

    UriExtractorType getExtractorType();

    String getUri(T target, String rawUrl);

}
