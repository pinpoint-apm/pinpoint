package com.nhn.pinpoint.common.util;


import org.junit.Assert;
import org.junit.Test;


/**
 * @author emeroad
 */
public class ApiDescriptionParserTest {
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



}
