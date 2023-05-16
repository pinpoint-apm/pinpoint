/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class AnnotationKeyUtilsTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    //    @Test
    public void intSize() {
//        2147483647
        logger.debug("{}", Integer.MAX_VALUE);
//        -2147483648
        logger.debug("{}", Integer.MIN_VALUE);
    }

    @Test
    public void isArgsKey() {
        Assertions.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS0.getCode()));
        Assertions.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGSN.getCode()));
        Assertions.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS5.getCode()));

        Assertions.assertFalse(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS0.getCode() + 1));
        Assertions.assertFalse(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGSN.getCode() - 1));
        Assertions.assertFalse(AnnotationKeyUtils.isArgsKey(Integer.MAX_VALUE));
        Assertions.assertFalse(AnnotationKeyUtils.isArgsKey(Integer.MIN_VALUE));

    }

    @Test
    public void isCachedArgsToArgs() {
        int i = AnnotationKeyUtils.cachedArgsToArgs(AnnotationKey.CACHE_ARGS0.getCode());
        Assertions.assertEquals(i, AnnotationKey.ARGS0.getCode());
    }
}
