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

package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.UriStatHistogramPoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Taejin Koo
 */
public class UriStatHistogramPointSerializerTest {

    @Test
    public void jsonTest() throws JsonProcessingException {
        int[] newArrayValue = UriStatHistogramBucket.createNewArrayValue();
        for (int i = 0; i < newArrayValue.length; i++) {
            newArrayValue[i] = ThreadLocalRandom.current().nextInt(0, 100);
        }

        long currentTimeMillis = System.currentTimeMillis();
        UriStatHistogramPoint point = new UriStatHistogramPoint(currentTimeMillis, newArrayValue);

        ObjectMapper om = new ObjectMapper();
        String jsonString = om.writeValueAsString(point);

        String arrayAsString = Arrays.toString(newArrayValue);
        String replace = StringUtils.replace(arrayAsString, " ", "");

        Assert.assertEquals(replace, jsonString);
    }

}
