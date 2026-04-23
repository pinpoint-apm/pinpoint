/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.db2;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

public final class Db2Constants {
    private Db2Constants() {
    }

    public static final String DB2_SCOPE = "DB2_SCOPE";

    public static final ServiceType DB2 = ServiceTypeProvider.getByName("DB2");
    public static final ServiceType DB2_EXECUTE_QUERY = ServiceTypeProvider.getByName("DB2_EXECUTE_QUERY");

    public static final String JCC_PACKAGE = "com.ibm.db2.jcc.am";

    public static final String JCC_DRIVER = "com.ibm.db2.jcc.DB2Driver";
    public static final String JCC_CONNECTION = "com.ibm.db2.jcc.am.Connection";
    public static final String JCC_CALLABLE_STATEMENT = "com.ibm.db2.jcc.am.CallableStatement";

    public static final String JCC_STATEMENT_INTERFACE = "com.ibm.db2.jcc.DB2Statement";
    public static final String JCC_PREPARED_STATEMENT_INTERFACE = "com.ibm.db2.jcc.DB2PreparedStatement";

    public static final String WAS_PREPARED_STATEMENT = "com.ibm.ws.rsadapter.jdbc.WSJccPreparedStatement";
}
