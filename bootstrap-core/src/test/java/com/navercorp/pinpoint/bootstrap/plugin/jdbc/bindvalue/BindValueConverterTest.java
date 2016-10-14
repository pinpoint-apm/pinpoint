/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue;

import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

public class BindValueConverterTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testBindValueToString() throws Exception {
        Date d = new Date();
        logger.debug("{}", d);

        byte[] bytes = new byte[] {1, 2, 4};
        String s = Arrays.toString(bytes);
        logger.debug(s);
    }

    @Test
    public void testBindValueBoolean() throws Exception {
        String setBoolean = BindValueConverter.convert("setBoolean", new Object[]{null, Boolean.TRUE});
        Assert.assertEquals(setBoolean, Boolean.TRUE.toString());
    }

    @Test
    public void testBindValueNotSupport() throws Exception {
        // Should not throw even if given arguments are not supported value
        String setBoolean = BindValueConverter.convert("setXxxx", new Object[]{null, "XXX"});
        Assert.assertEquals(setBoolean, "");
    }
}
