package com.nhn.pinpoint.common.mapping.code;

import com.nhn.pinpoint.common.mapping.ApiMappingTable;
import com.nhn.pinpoint.common.mapping.ClassMapping;
import com.nhn.pinpoint.common.mapping.MethodMapping;
import com.nhn.pinpoint.common.mapping.Register;

/**
 *
 */
public class ServerRegister implements Register {

    public static final int TomcatStandardHostValveCode = 5000;
    private static final ClassMapping TomcatStandardHostValve = new ClassMapping(TomcatStandardHostValveCode, "org.apache.catalina.core.StandardHostValve",
    		new MethodMapping("invoke", new String[] { "org.apache.catalina.connector.Request", "org.apache.catalina.connector.Response" }, new String[] { "request", "response" })
    );

    public static final int BlocHttpHandlerCode = 5010;
    private static final ClassMapping BlocHttpHandler = new ClassMapping(BlocHttpHandlerCode, "com.nhncorp.lucy.bloc.handler.HTTPHandler$BlocAdapter",
    		new MethodMapping("execute", new String[] { "external.org.apache.coyote.Request", "external.org.apache.coyote.Response" }, new String[] { "request", "response" })
    );
    
    public static final int SpringServletHandlerCode = 5020;
    private static final ClassMapping FrameworkServlet = new ClassMapping(SpringServletHandlerCode, "org.springframework.web.servlet.FrameworkServlet",
			new MethodMapping("doDelete", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[]{"request", "response"}),
			new MethodMapping("doGet", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
			new MethodMapping("doPost", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
			new MethodMapping("doPut", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" })
    		);
    
    public static final int HttpServletHandlerCode = 5030;
    private static final ClassMapping HttpServlet = new ClassMapping(HttpServletHandlerCode, "javax.servlet.http.HttpServlet",
			new MethodMapping("doDelete", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
			new MethodMapping("doGet", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
			new MethodMapping("doHead", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
    		new MethodMapping("doOptions", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
			new MethodMapping("doPost", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
			new MethodMapping("doPut", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" }),
			new MethodMapping("doTrace", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, new String[] { "request", "response" })
    		);

    @Override
    public void register(ApiMappingTable apiMappingTable, int startRange, int endRange) {
        apiMappingTable.put(TomcatStandardHostValve);
        apiMappingTable.put(BlocHttpHandler);
        apiMappingTable.put(FrameworkServlet);
        apiMappingTable.put(HttpServlet);
    }
}
