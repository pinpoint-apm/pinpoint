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

package com.navercorp.pinpoint.bootstrap.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
class DumpTypeTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void find() {
        DumpType none = DumpType.valueOf("ALWAYS");
        logger.debug("type:{}", none);
        Assertions.assertThrows(Exception.class, () -> {
            DumpType.valueOf("error");
        });
    }

    @Test
    void dumpType() {
        DumpType always = DumpType.of("ALWAYS");
        Assertions.assertEquals(DumpType.ALWAYS, always);

        DumpType always2 = DumpType.of("always");
        Assertions.assertEquals(DumpType.ALWAYS, always2);

        DumpType nullType = DumpType.of(null);
        Assertions.assertEquals(DumpType.EXCEPTION, nullType);

        DumpType exception = DumpType.of("exception");
        Assertions.assertEquals(DumpType.EXCEPTION, exception);
    }
}
