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

package com.navercorp.pinpoint.web.dao.hbase.filter;

import java.util.Arrays;

import org.junit.Test;

import com.navercorp.pinpoint.common.util.BytesUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrefixFilterTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void prefixInt() {

        byte[] before = new byte[4];

        for (int i = 1000; i < 1100; i++) {
            byte[] buffer = new byte[4];
            BytesUtils.writeVar32(i, buffer, 0);

            logger.debug(compare(before, buffer) + ", " + compare(buffer, before) + ", " + compare(buffer, buffer));

            before = Arrays.copyOf(buffer, 4);

            logger.debug(Arrays.toString(buffer));
        }
    }

    public int compare(byte[] left, byte[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }

}
