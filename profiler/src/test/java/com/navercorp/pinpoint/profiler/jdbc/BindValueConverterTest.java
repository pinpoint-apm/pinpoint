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

package com.navercorp.pinpoint.profiler.jdbc;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class BindValueConverterTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BindValueConverter bindValueConverter = BindValueConverter.defaultBindValueConverter();

    @Test
    public void testBindValueToString() {
        Date d = new Date();
        logger.debug("{}", d);

        byte[] bytes = new byte[] {1, 2, 4};
        String s = Arrays.toString(bytes);
        logger.debug(s);
    }

    @Test
    public void testBindValueBoolean() {
        String setBoolean = bindValueConverter.convert("setBoolean", new Object[]{null, Boolean.TRUE});
        Assert.assertEquals(setBoolean, Boolean.TRUE.toString());
    }

    @Test
    public void testBindValueNotSupport() {
        // Should not throw even if given arguments are not supported value
        String setBoolean = bindValueConverter.convert("setXxxx", new Object[]{null, "XXX"});
        Assert.assertEquals(setBoolean, "");
    }

    @Test
    public void testBindValueBytes() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        byte[] bytes = bb.array();

        String uuidHex = uuid.toString().toUpperCase().replaceAll("-", "");
        BindValueConverter bindValueConverter = BindValueConverter.defaultBindValueConverter();
        bindValueConverter.setHexBytesConverter();
        String setBytes = bindValueConverter.convert("setBytes", new Object[]{null, bytes});
        Assert.assertEquals(setBytes, uuidHex);
    }

    @Test
    public void testMaxWidth() {
        int maxWidth = 2;
        BindValueConverter converter = BindValueConverter.defaultBindValueConverter(maxWidth);

        String stringValue = converter.convert("setString", new Object[]{null, "abc"});
        Assert.assertEquals(StringUtils.abbreviate("abc", maxWidth), stringValue);
    }
}
