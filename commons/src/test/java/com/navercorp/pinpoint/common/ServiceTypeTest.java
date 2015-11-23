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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class ServiceTypeTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void findDesc() {
        DefaultServiceTypeRegistryService serviceTypeRegistryService = new DefaultServiceTypeRegistryService();
        String desc = "UNKNOWN_DB";
        List<ServiceType> serviceTypeList = serviceTypeRegistryService.findDesc(desc);
        boolean find = false;
        for (ServiceType serviceType : serviceTypeList) {
            if(serviceType.getDesc().equals(desc)) {
                find = true;
            }
        }
        Assert.assertTrue(find);

        try {
            serviceTypeList.add(ServiceType.INTERNAL_METHOD);
            Assert.fail();
        } catch (Exception ignored) {
        }
    }



}
