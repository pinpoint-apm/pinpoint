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

package com.navercorp.pinpoint.common.util;


/**
 * @author emeroad
 */
public class DefaultParsingResult implements ParsingResult {
    public static final char SEPARATOR = ',';
    private String sql;
    private StringBuilder output;
    private int id;


    public DefaultParsingResult() {

    }

    public DefaultParsingResult(String sql, StringBuilder output) {
        this.output = output;
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getOutput() {
        if (output == null) {
            return "";
        }
        return output.toString();
    }

    /**
     * 최초 한번은 불려야 된다 안불리고 appendOutputParam을 호출하면 nullpointer exception
     */
    void appendOutputSeparator() {
        if (output == null) {
            this.output = new StringBuilder();
        } else {
            this.output.append(SEPARATOR);
        }
    }

    void appendOutputParam(String str) {
        this.output.append(str);
    }

    void appendSeparatorCheckOutputParam(char ch) {
        if (ch == ',') {
            this.output.append(",,");
        } else {
            this.output.append(ch);
        }
    }

    void appendOutputParam(char ch) {
        this.output.append(ch);
    }

    @Override
    public String toString() {
        return "ParsingResult{" +
                "sql='" + sql + '\'' +
                ", output=" + output +
                '}';
    }
}
