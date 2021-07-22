/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfileLogConfigResolverTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void getLogPath() {
        URL resource = getClass().getClassLoader().getResource("log4j2-test.xml");
        logger.debug("{}", resource);
    }
}