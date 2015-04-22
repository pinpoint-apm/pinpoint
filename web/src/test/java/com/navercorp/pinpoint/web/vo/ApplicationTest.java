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

package com.navercorp.pinpoint.web.vo;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 */
public class ApplicationTest {
    @Test
    public void testEquals() throws Exception {
        Application one = new Application("test", ServiceType.STAND_ALONE);
        Application two = new Application("test", ServiceType.STAND_ALONE);

        Assert.assertTrue(one.equals(two));

        Assert.assertTrue(one.equals(two.getName(), two.getServiceType()));

        Assert.assertFalse(one.equals("test2", two.getServiceType()));
        Assert.assertFalse(one.equals("test", ServiceType.INTERNAL_METHOD));
        Assert.assertFalse(one.equals("test2", ServiceType.STAND_ALONE));

    }
}
