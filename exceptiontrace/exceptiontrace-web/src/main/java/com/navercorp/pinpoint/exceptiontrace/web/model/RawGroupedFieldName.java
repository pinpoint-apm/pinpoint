/*
 * Copyright 2024 NAVER Corp.
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

/**
 * @author intr3p1d
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RawGroupedFieldName {
    private String uriTemplate;
    private String errorClassName;
    private String errorMessage_logtype;
    private String stackTraceHash;


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

    public String getErrorMessage_logtype() {
        return errorMessage_logtype;
    }

    public void setErrorMessage_logtype(String errorMessage_logtype) {
        this.errorMessage_logtype = errorMessage_logtype;
    }

    public String getStackTraceHash() {
        return stackTraceHash;
    }

    public void setStackTraceHash(String stackTraceHash) {
        this.stackTraceHash = stackTraceHash;
    }


    @Override
    public String toString() {
        return "RawGroupedFieldName{" +
                "uriTemplate='" + uriTemplate + '\'' +
                ", errorClassName='" + errorClassName + '\'' +
                ", errorMessage_logtype='" + errorMessage_logtype + '\'' +
                ", stackTraceHash='" + stackTraceHash + '\'' +
                '}';
    }
}
