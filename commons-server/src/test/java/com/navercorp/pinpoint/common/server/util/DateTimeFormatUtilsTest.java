/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.util.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DateTimeFormatUtilsTest {


    @Test
    public void format() {
        long time = System.currentTimeMillis();
        Assert.assertEquals(DateUtils.longToDateStr(time), DateTimeFormatUtils.format(time));
    }

    @Test
    public void formatSimple() {
        long time = System.currentTimeMillis();
        String actual = DateTimeFormatUtils.formatSimple(time);
        Assert.assertEquals(DateUtils.longToDateStr(time, DateTimeFormatUtils.SIMPLE_DATE_FORMAT), actual);
    }

    @Test
    public void parseSimple() throws ParseException {
        String simpleDate = DateTimeFormatUtils.formatSimple(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat(DateTimeFormatUtils.SIMPLE_DATE_FORMAT);
        long time = format.parse(simpleDate).getTime();
        
        Assert.assertEquals(time, DateTimeFormatUtils.parseSimple(simpleDate));
    }



}