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

package com.navercorp.pinpoint.bootstrap.plugin.monitor;

import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorType;

/**
 * @author Taejin Koo
 */
public interface RequestUrlMappingExtractor<T> {

    //  '<' '>' are not allowed in URL Path.
    // Therefore,  '/<NOT_FOUND_MAPPING>" Path cannot be the same as any URL.
    String NOT_FOUNDED_MAPPING = "/<NOT_FOUND_MAPPING>";

    String[] DEFAULT_OFTEN_USED_URL = {"/", "/index.html"};

    RequestUrlMappingExtractorType getType();

    String getUrlMapping(T target, String rawUrl);

}

