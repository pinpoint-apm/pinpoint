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
import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
        Assert.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS0.getCode()));
        Assert.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGSN.getCode()));
        Assert.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS5.getCode()));

        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS0.getCode() +1));
        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGSN.getCode() -1));
        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(Integer.MAX_VALUE));
        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(Integer.MIN_VALUE));

    }

    @Test
    public void isCachedArgsToArgs() {
        int i = AnnotationKeyUtils.cachedArgsToArgs(AnnotationKey.CACHE_ARGS0.getCode());
        Assert.assertEquals(i, AnnotationKey.ARGS0.getCode());
    }
}
