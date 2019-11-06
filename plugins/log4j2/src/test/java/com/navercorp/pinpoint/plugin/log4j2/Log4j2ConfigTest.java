/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.log4j2;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author minwoo.jung
 */
public class Log4j2ConfigTest {

    @Test
    public void testLog4j2Config() {
        ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        Log4j2Config log4j2Config = new Log4j2Config(profilerConfig);
        Assert.assertTrue(!StringUtils.isEmpty(log4j2Config.toString()));
        Assert.assertTrue(!log4j2Config.isLog4j2LoggingTransactionInfo());
    }

}