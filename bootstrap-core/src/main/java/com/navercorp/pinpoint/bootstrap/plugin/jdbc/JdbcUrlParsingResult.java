/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * @author Taejin Koo
 */
public class JdbcUrlParsingResult {

    private final boolean success;
    private final DatabaseInfo databaseInfo;

    public JdbcUrlParsingResult(DatabaseInfo databaseInfo) {
        this(true, databaseInfo);
    }

    public JdbcUrlParsingResult(boolean success, DatabaseInfo databaseInfo) {
        this.success = success;
        this.databaseInfo = databaseInfo;
    }

    public boolean isSuccess() {
        return success;
    }

    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JdbcUrlParsingResult{");
        sb.append("success=").append(success);
        sb.append(", databaseInfo=").append(databaseInfo);
        sb.append('}');
        return sb.toString();
    }

}
