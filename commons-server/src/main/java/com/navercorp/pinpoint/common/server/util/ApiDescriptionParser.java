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

import com.navercorp.pinpoint.common.util.ApiDescription;
import com.navercorp.pinpoint.common.util.DefaultApiDescription;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Similar to MethodDescriptor, but instead parses string-based values.
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
        ApiDescription api = new DefaultApiDescription();
        api.setClassName(className);

        String methodName = parseMethodName(apiDescriptionString, methodStart, classIndex);
        api.setMethodName(methodName);

        String parameterDescriptor = apiDescriptionString.substring(methodStart + 1, methodEnd);
        String[] parameterList = parseParameter(parameterDescriptor);
        String[] simpleParameterList = parseSimpleParameter(parameterList);
        api.setSimpleParameter(simpleParameterList);

        int lineIndex = apiDescriptionString.lastIndexOf(':');
        // TODO for now, check and display the lineNumber
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
        if (ArrayUtils.isEmpty(parameterList)) {
            return EMPTY_STRING_ARRAY;
        }
        String[] simple = new String[parameterList.length];
        for (int i = 0; i < parameterList.length; i++) {
            simple[i] = simpleParameter(parameterList[i]);
        }
        return simple;
    }

    private String simpleParameter(String parameter) {
        int packageIndex = parameter.lastIndexOf(DOT);
        if (packageIndex == -1) {
            // same logic as below (-1 + 1 == 0) - explicitly checks as there may be changes in the future.
            packageIndex = 0;
        } else {
            packageIndex += 1;
        }


        return parameter.substring(packageIndex, parameter.length());
    }

    private String[] parseParameter(String parameterDescriptor) {
        if (StringUtils.isEmpty(parameterDescriptor)) {
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
