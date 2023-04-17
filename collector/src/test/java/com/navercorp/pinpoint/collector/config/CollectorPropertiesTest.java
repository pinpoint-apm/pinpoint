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

package com.navercorp.pinpoint.collector.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
@ContextConfiguration(classes = CollectorProperties.class)
@TestPropertySource(properties = "collector.l4.ip=127.0.0.1, 123.123.123.123")
@ExtendWith(SpringExtension.class)
public class CollectorPropertiesTest {

    @Autowired
    CollectorProperties collectorProperties;

    @Test
    public void l4IpTest() {
        assertThat(collectorProperties.getL4IpList())
                .contains("127.0.0.1", "123.123.123.123");
    }

}
