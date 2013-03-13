package com.profiler.common.util;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * MethodDescriptor 과 비슷한데. 문자열을 기반으로 parsing하여 생성하므로 따로 만들었음.
 * */
public class ApiDescriptionParser {

    private static final char DOT = '.';
    private static final char METHOD_PARAM_START = '(';
    private static final char METHOD_PARAM_END = ')';
    private static final char PARAMETER_SP = ',';
    private static Pattern PARAMETER_REGEX = Pattern.compile(", |,");
    //  org.springframework.web.servlet.FrameworkServlet.doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
// com.mysql.jdbc.ConnectionImpl.setAutoCommit(boolean autoCommitFlag)
//        com.mysql.jdbc.ConnectionImpl.commit()

    // org.apache.catalina.core.StandardHostValve.invoke(org.apache.catalina.connector.Request request, org.apache.catalina.connector.Response response):110
    public ApiDescription parse(String apiDescriptionString) {
        final int methodStart = apiDescriptionString.lastIndexOf(METHOD_PARAM_START);
        final int methodEnd = apiDescriptionString.lastIndexOf(METHOD_PARAM_END);
        final int classIndex = apiDescriptionString.lastIndexOf(DOT, methodStart);

        String className = parseClassName(apiDescriptionString, classIndex);
        ApiDescription api = new ApiDescription();
        api.setClassName(className);

        String methodName = parseMethodName(apiDescriptionString, methodStart, classIndex);
        api.setMethodName(methodName);

        String parameterDescriptor = apiDescriptionString.substring(methodStart + 1, methodEnd);
        String[] parameterList = parseParameter(parameterDescriptor);
        String[] simpleParameterList = parseSimpleParameter(parameterList);
        api.setSimpleParameter(simpleParameterList);
        return api;
    }

    private String[] parseSimpleParameter(String[] parameterList) {
        String[] simple = new String[parameterList.length];
        for(int i = 0; i<parameterList.length; i++) {
            simple[i] = simepleParameter(parameterList[i]);
        }
        return simple;
    }

    private String simepleParameter(String parameter) {
        int className = parameter.lastIndexOf(DOT) + 1;
        return parameter.substring(className, parameter.length());
    }

    private String[] parseParameter(String parameterDescriptor) {
        return PARAMETER_REGEX.split(parameterDescriptor);

    }

    private String parseClassName(String apiDescriptionString, int classIndex) {
        return apiDescriptionString.substring(0, classIndex);
    }

    private String parseMethodName(String apiDescriptionString, int methodStart, int classIndex) {
        return apiDescriptionString.substring(classIndex + 1, methodStart);
    }

}
