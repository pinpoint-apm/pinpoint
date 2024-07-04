/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.recorder.uri;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UriTemplateFilterTest {

    @Test
    public void filter() {
        UriTemplateFilter uriTemplateFilter = new UriTemplateFilter();
        assertEquals("/**", uriTemplateFilter.filter("/NULL"));

        // pass
        assertEquals("", uriTemplateFilter.filter(""));
        assertEquals("NULL", uriTemplateFilter.filter("NULL"));
        assertEquals("/request", uriTemplateFilter.filter("/request"));
        assertEquals("/**", uriTemplateFilter.filter("/**"));

        assertEquals(null, uriTemplateFilter.filter(null));
    }
}