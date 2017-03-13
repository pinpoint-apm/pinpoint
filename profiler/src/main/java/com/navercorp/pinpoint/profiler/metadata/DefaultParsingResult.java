/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.metadata;


import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.profiler.metadata.ParsingResultInternal;

/**
 * @author emeroad
 */
public class DefaultParsingResult implements ParsingResultInternal {

    private String originalSql;
    private String sql ;
    private String output;

    private int id = ParsingResult.ID_NOT_EXIST;


    public DefaultParsingResult(String originalSql) {
        this.originalSql = originalSql;
    }


    public String getOriginalSql() {
        return originalSql;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public int getId() {
        return id;
    }


    public boolean setId(int id) {
        // clear originalSql reference
        this.originalSql = null;

        if (this.id == ID_NOT_EXIST) {
            this.id = id;
            return true;
        }
        return false;
    }

    @Override
    public String getOutput() {
        if (this.output == null) {
            return "";
        }
        return this.output;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultParsingResult{");
        sb.append("sql='").append(sql).append('\'');
        sb.append(", output=").append(output);
        sb.append(", id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
