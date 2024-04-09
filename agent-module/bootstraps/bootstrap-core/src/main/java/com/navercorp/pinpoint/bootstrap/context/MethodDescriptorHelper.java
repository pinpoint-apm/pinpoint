/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.context;

public class MethodDescriptorHelper {

    public static MethodDescriptor apiId(final int apiId) {
        return new MethodDescriptor() {
            @Override
            public String getMethodName() {
                return "";
            }

            @Override
            public String getClassName() {
                return "";
            }

            @Override
            public String[] getParameterTypes() {
                return new String[0];
            }

            @Override
            public String[] getParameterVariableName() {
                return new String[0];
            }

            @Override
            public String getParameterDescriptor() {
                return "";
            }

            @Override
            public int getLineNumber() {
                return 0;
            }

            @Override
            public String getFullName() {
                return "";
            }

            @Override
            public void setApiId(int apiId) {
            }

            @Override
            public int getApiId() {
                return apiId;
            }

            @Override
            public String getApiDescriptor() {
                return "";
            }

            @Override
            public int getType() {
                return 0;
            }

            @Override
            public String toString() {
                return "{" +
                        "apiId=" + apiId +
                        '}';
            }
        };
    }
}
