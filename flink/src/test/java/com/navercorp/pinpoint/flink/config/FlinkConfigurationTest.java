/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.flink.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Woonduk Kang(emeroad)
 */
@TestPropertySource(locations = "classpath:profiles/local/pinpoint-flink.properties")
@ContextConfiguration(classes = FlinkConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FlinkConfigurationTest {
    @Autowired
    FlinkConfiguration flinkConfiguration;

    @Test
    public void log() {
        flinkConfiguration.log();
    }

}