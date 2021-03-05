/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin.test;

import java.util.Arrays;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringJoiner;

/**
 * @author Jongho Moon
 *
 */
public class ExpectedSql extends ExpectedAnnotation {
    private final String output;
    private final Object[] bindValues;

    public ExpectedSql(String query, String output, Object[] bindValues) {
        super(AnnotationKey.SQL_ID.getName(), query);
        this.output = output;
        this.bindValues = bindValues;
    }

    public String getQuery() {
        return (String) getValue();
    }

    public String getOutput() {
        return output;
    }

    public Object[] getBindValues() {
        return bindValues;
    }

    public String getBindValuesAsString() {
        if (bindValues.length == 0) {
            return null;
        }
        return join(bindValues, ", ");
    }

    private String join(Object[] objects, String delimiter) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Object object : objects) {
            joiner.add(String.valueOf(object));
        }
        return joiner.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getKeyName());
        builder.append("=[query=");
        builder.append(getQuery());
        builder.append(", output=");
        builder.append(output);
        builder.append(", bindValues");
        builder.append(Arrays.toString(bindValues));
        builder.append("]");

        return builder.toString();
    }
}