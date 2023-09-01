/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author intr3p1d
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupedFieldName {

    private String uriTemplate;
    private String errorClassName;
    private String errorMessage;
    private String stackTraceHash;

    public GroupedFieldName() {
    }

    public String inAString() {
        return Stream.of(uriTemplate, errorClassName, errorMessage, stackTraceHash)
                .filter(StringUtils::hasLength)
                .collect(Collectors.joining(", "));
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getErrorClassName() {
        return errorClassName;
    }

    public void setErrorClassName(String errorClassName) {
        this.errorClassName = errorClassName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTraceHash() {
        return stackTraceHash;
    }

    public void setStackTraceHash(String stackTraceHash) {
        this.stackTraceHash = stackTraceHash;
    }

    @Override
    public String toString() {
        return "GroupedFieldName{" +
                "uriTemplate='" + uriTemplate + '\'' +
                ", errorClassName='" + errorClassName + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", stackTraceHash='" + stackTraceHash + '\'' +
                '}';
    }
}
