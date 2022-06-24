/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class SearchDepthTest {

    @Test
    public void testSearchDepth() {
        SearchDepth zero = new SearchDepth(2);

        Assertions.assertEquals(zero.getDepth(), 0);
        Assertions.assertFalse(zero.isDepthOverflow());

        SearchDepth oneDepth = zero.nextDepth();
        Assertions.assertEquals(oneDepth.getDepth(), 1);
        Assertions.assertFalse(oneDepth.isDepthOverflow());

        SearchDepth twoDepth = oneDepth.nextDepth();
        Assertions.assertEquals(twoDepth.getDepth(), 2);
        Assertions.assertTrue(twoDepth.isDepthOverflow());
    }

}