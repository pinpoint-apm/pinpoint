/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.util.etag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ETagUtilsTest {

    @Test
    public void parse_weak() {
        ETag eTag = ETagUtils.parseETag("W/\"0815\"");
        Assertions.assertEquals("0815", eTag.getTag());
        Assertions.assertTrue(eTag.isWeak());
    }

    @Test
    public void parse_string() {
        ETag eTag = ETagUtils.parseETag("\"0815\"");
        Assertions.assertEquals("0815", eTag.getTag());
        Assertions.assertFalse(eTag.isWeak());
    }
}