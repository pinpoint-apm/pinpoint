/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.db.oracle;

/**
 * @author HyunGil Jeong
 */
public class OracleClassConstants {

    private OracleClassConstants() {}
    
    public static final String ORACLE_STATEMENT = "oracle/jdbc/driver/OracleStatement";
    public static final String ORACLE_STATEMENT_WRAPPER = "oracle/jdbc/driver/OracleStatementWrapper";
    
    public static final String ORACLE_PREPARED_STATEMENT = "oracle/jdbc/driver/OraclePreparedStatement";
    public static final String ORACLE_PREPARED_STATEMENT_WRAPPER = "oracle/jdbc/driver/OraclePreparedStatementWrapper";
    
}
