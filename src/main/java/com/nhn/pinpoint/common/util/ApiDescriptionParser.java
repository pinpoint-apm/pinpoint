package com.nhn.pinpoint.common.util;

import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * MethodDescriptor 과 비슷한데. 문자열을 기반으로 parsing하여 생성하므로 따로 만들었음.
 * @author emeroad
 */
public class ApiDescriptionParser {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
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
        if (apiDescriptionString == null) {
            throw new NullPointerException("apiDescriptionString must not be null");
        }

        final int methodStart = apiDescriptionString.lastIndexOf(METHOD_PARAM_START);
        if (methodStart == -1) {
            throw new IllegalArgumentException("'(' not found. invalid apiDescriptionString:" + apiDescriptionString);
        }

        final int methodEnd = apiDescriptionString.lastIndexOf(METHOD_PARAM_END);
        if (methodEnd == -1) {
            throw new IllegalArgumentException("')' not found. invalid apiDescriptionString:" + apiDescriptionString);
        }

        final int classIndex = apiDescriptionString.lastIndexOf(DOT, methodStart);
        if (classIndex == -1) {
            throw new IllegalArgumentException("'.' not found. invalid apiDescriptionString:" + apiDescriptionString);
        }

        String className = parseClassName(apiDescriptionString, classIndex);
        ApiDescription api = new ApiDescription();
        api.setClassName(className);

        String methodName = parseMethodName(apiDescriptionString, methodStart, classIndex);
        api.setMethodName(methodName);

        String parameterDescriptor = apiDescriptionString.substring(methodStart + 1, methodEnd);
        String[] parameterList = parseParameter(parameterDescriptor);
        String[] simpleParameterList = parseSimpleParameter(parameterList);
        api.setSimpleParameter(simpleParameterList);

        int lineIndex = apiDescriptionString.lastIndexOf(':');
        // 일단 땜방으로 lineNumber체크해서 lineNumber를 뿌려주도록 하자.
        if (lineIndex != -1) {
            try {
                int line = Integer.parseInt(apiDescriptionString.substring(lineIndex + 1, apiDescriptionString.length()));
                api.setLine(line);
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(this.getClass()).warn("line number parse error {}", e);
            }
        }

        return api;
    }

    private String[] parseSimpleParameter(String[] parameterList) {
        if (parameterList == null || parameterList.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        String[] simple = new String[parameterList.length];
        for (int i = 0; i < parameterList.length; i++) {
            simple[i] = simepleParameter(parameterList[i]);
        }
        return simple;
    }

    private String simepleParameter(String parameter) {
        int packageIndex = parameter.lastIndexOf(DOT);
        if (packageIndex == -1) {
            // 없을 경우 아래 로직가 동일하나 추후 뭔가 변경사항이 생길수 있어 명시적으로 체크하는 로직으로 구현.
            packageIndex = 0;
        } else {
            packageIndex += 1;
        }


        return parameter.substring(packageIndex, parameter.length());
    }

    private String[] parseParameter(String parameterDescriptor) {
        if (parameterDescriptor == null || parameterDescriptor.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        return PARAMETER_REGEX.split(parameterDescriptor);

    }

    private String parseClassName(String apiDescriptionString, int classIndex) {
        return apiDescriptionString.substring(0, classIndex);
    }

    private String parseMethodName(String apiDescriptionString, int methodStart, int classIndex) {
        return apiDescriptionString.substring(classIndex + 1, methodStart);
    }

}
