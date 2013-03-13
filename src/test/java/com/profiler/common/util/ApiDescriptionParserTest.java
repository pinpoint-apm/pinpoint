package com.profiler.common.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
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




    }

}
