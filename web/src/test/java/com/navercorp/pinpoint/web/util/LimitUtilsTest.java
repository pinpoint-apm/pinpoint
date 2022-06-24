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

package com.navercorp.pinpoint.web.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class LimitUtilsTest {
    @Test
    public void testCheckLimit() {
        int equals = LimitUtils.checkRange(LimitUtils.MAX);
        Assertions.assertEquals(equals, LimitUtils.MAX);

        int over = LimitUtils.checkRange(LimitUtils.MAX + 1);
        Assertions.assertEquals(over, LimitUtils.MAX);

        int low = LimitUtils.checkRange(0);
        Assertions.assertEquals(low, 0);

        try {
            LimitUtils.checkRange(-1);
            Assertions.fail();
        } catch (Exception ignored) {
        }

    }
}
