/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.service.web.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author minwoo.jung
 */
class ServiceNameTest {

    @Test
    void testServiceNameCreation() {
        String serviceName = "testService";
        ServiceName serviceNameObj = new ServiceName(serviceName);

        assertThat(serviceNameObj).isNotNull();
        assertThat(serviceNameObj.getName()).isEqualTo(serviceName);
    }

    @Test
    void testServiceNameWithDifferentNames() {
        ServiceName serviceName1 = new ServiceName("service1");
        ServiceName serviceName2 = new ServiceName("service2");

        assertThat(serviceName1.getName()).isEqualTo("service1");
        assertThat(serviceName2.getName()).isEqualTo("service2");
        assertThat(serviceName1.getName()).isNotEqualTo(serviceName2.getName());
    }

    @Test
    void testToString() {
        String serviceName = "myService";
        ServiceName serviceNameObj = new ServiceName(serviceName);

        String result = serviceNameObj.toString();

        assertThat(result).contains("ServiceName");
        assertThat(result).contains("myService");
    }

    @Test
    void testServiceNameWithDefaultConstant() {
        ServiceName serviceName = new ServiceName(ServiceConstants.DEFAULT);

        assertThat(serviceName.getName()).isEqualTo(ServiceConstants.DEFAULT);
        assertThat(serviceName.getName()).isEqualTo("DEFAULT");
    }
}
