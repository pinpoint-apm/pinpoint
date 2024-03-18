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

/**
 * @author emeroad
 */
public abstract class ParsingResultInternal<ID> implements ParsingResult {
    private String originalSql;
    private String sql;
    private String output;

    protected ParsingResultInternal(String originalSql) {
        this.originalSql = originalSql;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public String getOutput() {
        if (output == null) {
            return "";
        }
        return output;
    }

    public String getOriginalSql() {
        return originalSql;
    }

    public void clearOriginalSql() {
        this.originalSql = null;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public abstract ID getId();

    public abstract boolean setId(ID id);
}
