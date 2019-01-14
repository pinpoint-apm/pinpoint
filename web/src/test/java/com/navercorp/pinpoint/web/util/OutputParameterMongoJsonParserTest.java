/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Roy Kim
 */
public class OutputParameterMongoJsonParserTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final OutputParameterMongoJsonParser parser = new OutputParameterMongoJsonParser();

    @Test
    public void testParseOutputParameter() throws Exception {

        assertOutputParameter("\"wow\",12,34", "\"wow\"", "12", "34");

        assertOutputParameter("\"\"\"a\",\"b\"", "\"\"a\"", "\"b\"");

        assertOutputParameter("\"\"\"\"\"a\",\"b\"", "\"\"\"a\"", "\"b\"");

        assertOutputParameter("\",\"\",a\",\"b\",c", "\",\",a\"", "\"b\"", "c");

        assertOutputParameter("\"a\",\"\"", "\"a\"", "\"\"");

        assertOutputParameter("\"a\",", "\"a\"", "");

        assertOutputParameter("\"wow\",\"12,\"34\"", "\"wow\"", "\"12,\"34\"");

        assertOutputParameter("");

    }

    private void assertOutputParameter(String outputParam, Object... params) {
        List<String> result = parser.parseOutputParameter(outputParam);
        logger.debug("parseResult size:{} data:{}", result.size(), result);
        try {
            Assert.assertArrayEquals(params, result.toArray(new String[0]));
        } catch (AssertionError e) {
            logger.warn("parseResult:{}", result);
            logger.warn("params:{}", (Object[]) params);
            throw e;
        }
    }
}
