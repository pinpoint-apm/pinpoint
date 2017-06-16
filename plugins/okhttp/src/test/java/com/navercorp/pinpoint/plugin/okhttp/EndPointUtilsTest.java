/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.okhttp;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EndPointUtilsTest {
    @Test
    public void getPort() throws Exception {
        Assert.assertEquals(EndPointUtils.getPort(-1, 80), -1);
        Assert.assertEquals(EndPointUtils.getPort(80, 80), -1);
        Assert.assertEquals(EndPointUtils.getPort(80, 8080), 80);
    }

}