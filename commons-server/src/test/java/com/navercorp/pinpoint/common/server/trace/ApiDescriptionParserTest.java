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

package com.navercorp.pinpoint.common.server.trace;


import com.navercorp.pinpoint.common.util.LineNumber;
import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 */
public class ApiDescriptionParserTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApiDescriptionParser apiParser = new ApiDescriptionParser();

    @Test
    public void parse() {
// org.springframework.web.servlet.FrameworkServlet.doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
// com.mysql.jdbc.ConnectionImpl.setAutoCommit(boolean autoCommitFlag)
// com.mysql.jdbc.ConnectionImpl.commit()
// org.apache.catalina.core.StandardHostValve.invoke(org.apache.catalina.connector.Request request, org.apache.catalina.connector.Response response):110
        String api = "a.StandardHostValve.invoke(b.Request request, b.Response response)";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("a.StandardHostValve", result.getClassName());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());
        Assert.assertEquals("a", result.getPackageName());

        Assert.assertEquals("invoke", result.getMethodName());

        Assert.assertEquals("invoke(Request request, Response response)", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{"Request request", "Response response"}, result.getSimpleParameter());
    }

    @Test
    public void parseNoArgs() {
        String api = "a.StandardHostValve.invoke()";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("a.StandardHostValve", result.getClassName());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());
        Assert.assertEquals("a", result.getPackageName());

        Assert.assertEquals("invoke", result.getMethodName());

        Assert.assertEquals("invoke()", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{}, result.getSimpleParameter());
    }


    @Test
    public void parseNoPackage() {

        String api = "StandardHostValve.invoke(Request request, Response response)";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("StandardHostValve", result.getClassName());
        Assert.assertEquals("StandardHostValve", result.getSimpleClassName());
        Assert.assertEquals("", result.getPackageName());

        Assert.assertEquals("invoke", result.getMethodName());

        Assert.assertEquals("invoke(Request request, Response response)", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{"Request request", "Response response"}, result.getSimpleParameter());

    }


    @Test
    public void parseTag() {
        final String apiDescriptionString = ".Tomcat Servlet Process()";
        ApiDescription result = apiParser.parse(apiDescriptionString, -1);
        
        logger.debug(result.getMethodDescription());
        logger.debug(result.getSimpleClassName());
    }


    @Test
    public void parseNoClass() {
        String api = "function()";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("", result.getClassName());
        Assert.assertEquals("", result.getSimpleClassName());
        Assert.assertEquals("", result.getPackageName());

        Assert.assertEquals("function", result.getMethodName());

        Assert.assertEquals("function()", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{}, result.getSimpleParameter());
    }

    @Test
    public void parseNoClass_classskip_trick() {
        String api = ".function()";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("", result.getClassName());
        Assert.assertEquals("", result.getSimpleClassName());
        Assert.assertEquals("", result.getPackageName());

        Assert.assertEquals("function", result.getMethodName());

        Assert.assertEquals("function()", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{}, result.getSimpleParameter());
    }


    @Test
    public void parse_noParameterTYpe() {
        String api = "express.app.get(path, callback)";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("express.app", result.getClassName());
        Assert.assertEquals("app", result.getSimpleClassName());
        Assert.assertEquals("express", result.getPackageName());

        Assert.assertEquals("get", result.getMethodName());

        Assert.assertEquals("get(path, callback)", result.getMethodDescription());


        Assert.assertArrayEquals(new String[]{"path", "callback"}, result.getSimpleParameter());
    }

    @Test
    public void parse_tailingInfo1() {
        String api = "express.app.get(path, callback) at /src/file:123";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("express.app", result.getClassName());
        Assert.assertEquals("app", result.getSimpleClassName());
        Assert.assertEquals("express", result.getPackageName());

        Assert.assertEquals("get", result.getMethodName());

        Assert.assertEquals("get(path, callback) at /src/file:123", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{"path", "callback"}, result.getSimpleParameter());
    }


    @Test
    public void parse_tailingInfo2() {
        String api = "express.app.get(path, callback) at /src/file:123";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("express.app", result.getClassName());
        Assert.assertEquals("app", result.getSimpleClassName());
        Assert.assertEquals("express", result.getPackageName());

        Assert.assertEquals("get", result.getMethodName());

        Assert.assertEquals("get(path, callback) at /src/file:123", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{"path", "callback"}, result.getSimpleParameter());
    }

    @Test
    public void parse_tailingInfo4() {
        String api = "express.app.get(path, callback) at (test.value)";
        ApiDescription result = apiParser.parse(api, LineNumber.NO_LINE_NUMBER);

        Assert.assertEquals("express.app", result.getClassName());
        Assert.assertEquals("app", result.getSimpleClassName());
        Assert.assertEquals("express", result.getPackageName());

        Assert.assertEquals("get", result.getMethodName());

        Assert.assertEquals("get(path, callback) at (test.value)", result.getMethodDescription());

        Assert.assertArrayEquals(new String[]{"path", "callback"}, result.getSimpleParameter());
    }
}
