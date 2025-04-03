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

package com.navercorp.pinpoint.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class MathUtilsTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void fastAbs() {
        Assertions.assertTrue(MathUtils.fastAbs(-1) > 0);
        Assertions.assertTrue(MathUtils.fastAbs(0) == 0);
        Assertions.assertTrue(MathUtils.fastAbs(1) > 0);
    }

    @Test
    public void overflow() {

        logger.debug("abs:{}", Math.abs(Integer.MIN_VALUE));
        logger.debug("fastabs:{}", MathUtils.fastAbs(Integer.MIN_VALUE));

        int index = Integer.MIN_VALUE - 2;
        for (int i = 0; i < 5; i++) {
            logger.debug("{}------------", i);
            logger.debug("{}", index);
            logger.debug("mod:{}", index % 3);
            logger.debug("abs:{}", Math.abs(index));
            logger.debug("fastabs:{}", MathUtils.fastAbs(index));

            index++;
        }
    }

}
