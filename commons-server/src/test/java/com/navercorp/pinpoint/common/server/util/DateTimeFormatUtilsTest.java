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

import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DateTimeFormatUtilsTest {

    private final DateFormat defaultDateFormatfinal = new SimpleDateFormat(DateTimeFormatUtils.DEFAULT_DATE_FORMAT);
    private final DateFormat simpleDateFormatfinal = new SimpleDateFormat(DateTimeFormatUtils.SIMPLE_DATE_FORMAT);

    @Test
    public void format() {
        long time = System.currentTimeMillis();
        Assert.assertEquals(defaultDateFormatfinal.format(time), DateTimeFormatUtils.format(time));
    }

    @Test
    public void formatSimple() {
        long time = System.currentTimeMillis();
        String actual = DateTimeFormatUtils.formatSimple(time);
        Assert.assertEquals(simpleDateFormatfinal.format(time), actual);
    }

    @Test
    public void parseSimple() throws ParseException {
        String simpleDate = DateTimeFormatUtils.formatSimple(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat(DateTimeFormatUtils.SIMPLE_DATE_FORMAT);
        long time = format.parse(simpleDate).getTime();
        
        Assert.assertEquals(time, DateTimeFormatUtils.parseSimple(simpleDate));
    }

    @Test(expected = DateTimeParseException.class)
    public void parseSimple_sqltimestamp_error() throws ParseException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        // "2100-12-31 23:59:59.111"
        String simpleDate = timestamp.toString();
        SimpleDateFormat format = new SimpleDateFormat(DateTimeFormatUtils.SIMPLE_DATE_FORMAT);

        long time = format.parse(simpleDate).getTime();
        DateTimeFormatUtils.parseSimple(simpleDate);
    }


}