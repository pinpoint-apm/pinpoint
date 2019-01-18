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

package com.navercorp.pinpoint.plugin.mongo;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NormalizedBson {

    private final String normalizedBson;
    private final String parameter;

    public NormalizedBson(String normalizedBson, String parameter) {
        this.normalizedBson = normalizedBson;
        this.parameter = parameter;
    }

    public String getNormalizedBson() {
        return normalizedBson;
    }

    public String getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        return "NormalizedBson{" +
                "normalizedBson='" + normalizedBson + '\'' +
                ", parameter='" + parameter + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NormalizedBson)) return false;

        NormalizedBson that = (NormalizedBson) o;

        if (normalizedBson != null ? !normalizedBson.equals(that.normalizedBson) : that.normalizedBson != null) return false;
        return parameter != null ? parameter.equals(that.parameter) : that.parameter == null;
    }

    @Override
    public int hashCode() {
        int result = normalizedBson != null ? normalizedBson.hashCode() : 0;
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        return result;
    }
}
