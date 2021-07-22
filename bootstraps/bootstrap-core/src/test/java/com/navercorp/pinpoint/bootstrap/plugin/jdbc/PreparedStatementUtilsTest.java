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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class PreparedStatementUtilsTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testBindSetMethod() {
        List<Method> bindVariableSetMethod = PreparedStatementUtils.findBindVariableSetMethod();
        for (Method method : bindVariableSetMethod) {
            logger.debug("{}", method);
        }
    }

    @Test
    public void testMatch() throws Exception {
        Assert.assertTrue(PreparedStatementUtils.isSetter("setNCString"));
        Assert.assertTrue(PreparedStatementUtils.isSetter("setInt"));
        Assert.assertTrue(PreparedStatementUtils.isSetter("setTestTeTst"));

    }
}
