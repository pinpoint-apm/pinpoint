/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.pluginit.jdbc;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface JDBCApi {

    JDBCDriverClass getJDBCDriverClass();

    DriverClass getDriver();

    interface DriverClass {
        /**
         * {@link java.sql.Driver#connect(String, Properties)}
         */
        Method getConnect();
    }


    ConnectionClass getConnection();

    interface ConnectionClass {
        /**
         * {@link Connection#setAutoCommit(boolean)}
         */
        Method getSetAutoCommit();

        /**
         * {@link Connection#prepareStatement(String)}
         */
        Method getPrepareStatement();

        /**
         * {@link Connection#prepareCall(String)}
         */
        Method getPrepareCall();

        /**
         * {@link Connection#commit()}
         */
        Method getCommit();
    }


    StatementClass getStatement();

    interface StatementClass {

        /**
         * {@link java.sql.Statement#executeQuery(String)}
         */
        Method getExecuteQuery();

        /**
         * {@link java.sql.Statement#executeUpdate(String)}
         */
        Method getExecuteUpdate();
    }


    PreparedStatementClass getPreparedStatement();

    interface PreparedStatementClass {

        /**
         * {@link PreparedStatement#execute()}
         */
        Method getExecute();

        /**
         * {@link PreparedStatement#executeQuery()};
         */
        Method getExecuteQuery();
    }


    CallableStatementClass getCallableStatement();

    interface CallableStatementClass {
        /**
         * {@link CallableStatement#registerOutParameter(int, int)}
         */
        Method getRegisterOutParameter();
        /**
         * {@link CallableStatement#execute()}
         */
        Method getExecute();
        /**
         * {@link CallableStatement#executeQuery()}
         */
        Method getExecuteQuery();
    }

}
