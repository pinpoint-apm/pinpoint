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

package com.navercorp.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Date;

/**
 * @author emeroad
 */
public class RuntimeMXBeanUtilsTest {

    @Test
    public void vmStartTime() {
        long vmStartTime = RuntimeMXBeanUtils.getVmStartTime();
        Assert.assertNotSame(0, vmStartTime);
    }

    @Test
    public void pid() {
        int pid = RuntimeMXBeanUtils.getPid();
        Assert.assertTrue(pid > 0);
    }
}
