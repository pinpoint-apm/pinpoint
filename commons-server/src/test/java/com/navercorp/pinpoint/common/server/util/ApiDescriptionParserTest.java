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

package com.navercorp.pinpoint.common.server.util;


import com.navercorp.pinpoint.common.server.util.ApiDescriptionParser;
import com.navercorp.pinpoint.common.util.ApiDescription;
import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 */
public class ApiDescriptionParserTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ApiDescriptionParser apiParser = new ApiDescriptionParser();

    @Test
    public void parse() {
// org.springframework.web.servlet.FrameworkServlet.doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
// com.mysql.jdbc.ConnectionImpl.setAutoCommit(boolean autoCommitFlag)
// com.mysql.jdbc.ConnectionImpl.commit()
// org.apache.catalina.core.StandardHostValve.invoke(org.apache.catalina.connector.Request request, org.apache.catalina.connector.Response response):110
        String api = "a.StandardHostValve.invoke(b.Request request, b.Response response)";
        ApiDescription result = apiParser.parse(api);

        Assert.assertEquals("a.StandardHostValve", result.getClassName());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());
        Assert.assertEquals("a", result.getPackageNameName());

        Assert.assertEquals("invoke", result.getMethodName());

        Assert.assertEquals("invoke(Request request, Response response)", result.getSimpleMethodDescription());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());

        Assert.assertArrayEquals(new String[]{"Request request", "Response response"}, result.getSimpleParameter());
    }


    @Test
    public void parseNoArgs() {
        String api = "a.StandardHostValve.invoke()";
        ApiDescription result = apiParser.parse(api);

        Assert.assertEquals("a.StandardHostValve", result.getClassName());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());
        Assert.assertEquals("a", result.getPackageNameName());

        Assert.assertEquals("invoke", result.getMethodName());

        Assert.assertEquals("invoke()", result.getSimpleMethodDescription());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());

        Assert.assertArrayEquals(new String[]{}, result.getSimpleParameter());
    }


    @Test
    public void parseNoPackage() {

        String api = "StandardHostValve.invoke(Request request, Response response)";
        ApiDescription result = apiParser.parse(api);

        Assert.assertEquals("StandardHostValve", result.getClassName());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());
        Assert.assertEquals("", result.getPackageNameName());

        Assert.assertEquals("invoke", result.getMethodName());

        Assert.assertEquals("invoke(Request request, Response response)", result.getSimpleMethodDescription());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());

        Assert.assertArrayEquals(new String[]{"Request request", "Response response"}, result.getSimpleParameter());

    }


    @Test
    public void parseTag() {
        final String apiDescriptionString = ".Tomcat Servlet Process()";
        ApiDescription result = apiParser.parse(apiDescriptionString);
        
        logger.debug(result.getSimpleMethodDescription());
        logger.debug(result.getSimpleClassName());
    }

}
