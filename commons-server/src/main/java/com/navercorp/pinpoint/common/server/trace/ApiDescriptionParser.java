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

import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Similar to MethodDescriptor, but instead parses string-based values.
 * @author emeroad
 */
public class ApiDescriptionParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final char DOT = '.';
    private static final char METHOD_PARAM_START = '(';
    private static final char METHOD_PARAM_END = ')';

    private static final char PARAMETER_SP = ',';
    private static final Pattern PARAMETER_REGEX = Pattern.compile(", |,");


    public ApiDescription parse(ApiMetaDataBo apiMetaDataBo) {
        return parse(apiMetaDataBo.getApiInfo(), apiMetaDataBo.getLineNumber());
    }

    //  org.springframework.web.servlet.FrameworkServlet.doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
// com.mysql.jdbc.ConnectionImpl.setAutoCommit(boolean autoCommitFlag)
//        com.mysql.jdbc.ConnectionImpl.commit()

    // org.apache.catalina.core.StandardHostValve.invoke(org.apache.catalina.connector.Request request, org.apache.catalina.connector.Response response):110
    public ApiDescription parse(String apiDescription, int lineNumber) {
        Objects.requireNonNull(apiDescription, "apiDescriptionString");

        final int methodStart = apiDescription.indexOf(METHOD_PARAM_START);
        if (methodStart == -1) {
            throw new IllegalArgumentException("'(' not found. invalid apiDescriptionString:" + apiDescription);
        }

        final int methodEnd = apiDescription.indexOf(METHOD_PARAM_END, methodStart);
        if (methodEnd == -1) {
            throw new IllegalArgumentException("')' not found. invalid apiDescriptionString:" + apiDescription);
        }

        final int classIndex = apiDescription.lastIndexOf(DOT, methodStart);
        String className = getClassName(apiDescription, classIndex);
        String methodName = parseMethodName(apiDescription, methodStart, classIndex);

        String parameterDescriptor = apiDescription.substring(methodStart + 1, methodEnd);
        String[] parameterList = parseParameter(parameterDescriptor);
        String[] simpleParameterList = parseSimpleParameter(parameterList);

        String trailing = parseTailingInfo(apiDescription, methodEnd);
//            int trailingLineNumber = trailing.lastIndexOf(':');
//            if (trailingLineNumber != -1) {
//                if (LineNumber.isNoLineNumber(lineNumber)) {
//                    lineNumber = NumberUtils.parseInteger(trailing.substring(trailingLineNumber), LineNumber.NO_LINE_NUMBER);
//                }
//            }

        return new DefaultApiDescription(apiDescription, className, methodName, simpleParameterList, trailing, lineNumber);
    }

    private String parseTailingInfo(String apiDescription, int methodEnd) {
        if (apiDescription.length() > methodEnd) {
            return apiDescription.substring(methodEnd + 1);
        }
        return "";
    }

    private String getClassName(String apiDescription, int classIndex) {
        if (classIndex == -1) {
            return "";
        } else {
            return parseClassName(apiDescription, classIndex);
        }
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
