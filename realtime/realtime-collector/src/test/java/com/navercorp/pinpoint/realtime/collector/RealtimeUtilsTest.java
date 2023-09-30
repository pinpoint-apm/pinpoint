/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.collector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static com.navercorp.pinpoint.realtime.collector.RealtimeUtils.COLLECTOR_ID;

/**
 * @author youngjin.kim2
 */
public class RealtimeUtilsTest {

    private static final Logger logger = LogManager.getLogger(RealtimeUtilsTest.class);

    @Test
    public void shouldFindHostname() {
        logger.debug("Hostname: {}", COLLECTOR_ID);
    }

}
