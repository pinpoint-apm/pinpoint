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

package com.navercorp.pinpoint.rpc.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

public class ListUtilsTest {

    @Test
    public void addIfValueNotNullTest() {
        List<String> list = new ArrayList<String>();
        
        ListUtils.addIfValueNotNull(list, "firstString");
        ListUtils.addIfValueNotNull(list, null);
        
        Assert.assertEquals(1, list.size());
    }
    
    @Test
    public void addAllIfAllValuesNotNullTest() {
        List<String> list = new ArrayList<String>();

        String[] values = {"firstString", null, "secondString"};
        
        ListUtils.addAllIfAllValuesNotNull(list, values);
        
        Assert.assertEquals(0, list.size());
    }
    
    @Test
    public void addAllExceptNullValueTest() {
        List<String> list = new ArrayList<String>();

        String[] values = {"firstString", null, "secondString"};
        
        ListUtils.addAllExceptNullValue(list, values);
        
        Assert.assertEquals(2, list.size());
    }
    
}
