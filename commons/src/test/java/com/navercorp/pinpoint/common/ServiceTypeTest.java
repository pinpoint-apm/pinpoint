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

package com.navercorp.pinpoint.common;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceTypeTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @BeforeClass
    public static void init() {
        ServiceTypeInitializer.initialize();
    }

    @Test
    public void findDesc() {
        String desc = "MYSQL";
        List<ServiceType> mysqlList = ServiceType.findDesc(desc);
        boolean find = false;
        for (ServiceType serviceType : mysqlList) {
            if(serviceType.getDesc().equals(desc)) {
                find = true;
            }
        }
        Assert.assertTrue(find);

        try {
            mysqlList.add(ServiceType.ARCUS);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void child() {
        ServiceType oracle = ServiceType.ORACLE;


    }

    @Test
    public void test() {
        for (ServiceType value : ServiceType.values()) {
            logger.debug(value.toString() + " " + value.getCode());
        }

    }

}
