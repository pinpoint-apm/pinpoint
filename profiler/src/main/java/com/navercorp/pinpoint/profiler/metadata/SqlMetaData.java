/*
 * Copyright 2018 NAVER Corp.
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

/**
 * @author Woonduk Kang(emeroad)
 */
public class SqlMetaData implements MetaDataType {

//    private java.lang.String agentId; // required
//    private long agentStartTime; // required
    private final int sqlId; // required
    private final String sql; // required

    public SqlMetaData(int sqlId, String sql) {
        this.sqlId = sqlId;
        this.sql = sql;
    }


    public int getSqlId() {
        return sqlId;
    }

    public String getSql() {
        return sql;
    }
}
