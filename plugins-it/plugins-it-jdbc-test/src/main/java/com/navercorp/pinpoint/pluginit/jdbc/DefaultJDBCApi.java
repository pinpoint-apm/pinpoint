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

import com.navercorp.pinpoint.common.util.Assert;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultJDBCApi implements JDBCApi {
    public final JDBCDriverClass jdbcDriverClass;

    public DefaultJDBCApi(JDBCDriverClass jdbcDriverClass) {
        this.jdbcDriverClass = Assert.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
    }

    @Override
    public JDBCDriverClass getJDBCDriverClass() {
        return jdbcDriverClass;
    }

    public DriverClass getDriver() {
        return new DefaultDriverClass(getJDBCDriverClass());
    }


    public static class DefaultDriverClass implements DriverClass {
        private final JDBCDriverClass jdbcDriverClass;

        public DefaultDriverClass(JDBCDriverClass jdbcDriverClass) {
            this.jdbcDriverClass = Assert.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
        }

        protected Class<Driver> getConnection() {
            return jdbcDriverClass.getDriver();
        }

        @Override
        public Method getConnect() {
            final Class<Driver> connection = getConnection();
            return getDeclaredMethod(connection, "connect", String.class, Properties.class);
        }
    }

    public ConnectionClass getConnection() {
        return new DefaultConnectionClass(getJDBCDriverClass());
    }


    public static class DefaultConnectionClass implements ConnectionClass {

        private final JDBCDriverClass jdbcDriverClass;

        public DefaultConnectionClass(JDBCDriverClass jdbcDriverClass) {
            this.jdbcDriverClass = Assert.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
        }

        protected Class<Connection> getConnection() {
            return jdbcDriverClass.getConnection();
        }

        @Override
        public Method getSetAutoCommit() {
            final Class<Connection> connection = getConnection();
            return getDeclaredMethod(connection, "setAutoCommit", boolean.class);
        }

        @Override
        public Method getPrepareStatement() {
            final Class<Connection> connection = getConnection();
            return getDeclaredMethod(connection, "prepareStatement", String.class);
        }

        @Override
        public Method getPrepareCall() {
            final Class<Connection> connection = getConnection();
            return getDeclaredMethod(connection, "prepareCall", String.class);
        }

        @Override
        public Method getCommit() {
            final Class<Connection> connection = getConnection();
            return getDeclaredMethod(connection, "commit");
        }
    }


    public StatementClass getStatement() {
        return new DefaultStatementClass(getJDBCDriverClass());
    }

    public static class DefaultStatementClass implements StatementClass {
        final JDBCDriverClass jdbcDriverClass;
        public DefaultStatementClass(JDBCDriverClass jdbcDriverClass) {
            this.jdbcDriverClass = Assert.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
        }

        protected Class<Statement> getStatement() {
            return jdbcDriverClass.getStatement();
        }

        @Override
        public Method getExecuteQuery() {
            final Class<Statement> statement = getStatement();
            return getDeclaredMethod(statement, "executeQuery", String.class);
        }
        @Override
        public Method getExecuteUpdate() {
            final Class<Statement> statement = getStatement();
            return getDeclaredMethod(statement, "executeUpdate", String.class);
        }
    }

    public PreparedStatementClass getPreparedStatement() {
        return new DefaultPreparedStatementClass(getJDBCDriverClass());
    }


    public static class DefaultPreparedStatementClass implements PreparedStatementClass {
        private final JDBCDriverClass jdbcDriverClass;

        public DefaultPreparedStatementClass(JDBCDriverClass jdbcDriverClass) {
            this.jdbcDriverClass = Assert.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
        }

        protected Class<PreparedStatement> getPreparedStatement() {
            return jdbcDriverClass.getPreparedStatement();
        }

        @Override
        public Method getExecute() {
            final Class<PreparedStatement> statement = getPreparedStatement();
            return getDeclaredMethod(statement, "execute");
        }

        @Override
        public Method getExecuteQuery() {
            final Class<PreparedStatement> statement = getPreparedStatement();
            return getDeclaredMethod(statement, "executeQuery");
        }
    }

    @Override
    public CallableStatementClass getCallableStatement() {
        return new DefaultCallableStatementClass(getJDBCDriverClass());
    }


    public static class DefaultCallableStatementClass implements CallableStatementClass {
        private final JDBCDriverClass jdbcDriverClass;

        public DefaultCallableStatementClass(JDBCDriverClass jdbcDriverClass) {
            this.jdbcDriverClass = Assert.requireNonNull(jdbcDriverClass, "jdbcDriverClass");
        }

        protected Class<CallableStatement> getCallableStatement() {
            return jdbcDriverClass.getCallableStatement();
        }

        @Override
        public Method getRegisterOutParameter() {
            final Class<CallableStatement> callableStatement = getCallableStatement();
            return getDeclaredMethod(callableStatement, "registerOutParameter", int.class, int.class);
        }

        @Override
        public Method getExecute() {
            final Class<CallableStatement> callableStatement = getCallableStatement();
            return getDeclaredMethod(callableStatement, "execute");
        }

        @Override
        public Method getExecuteQuery() {
            final Class<CallableStatement> callableStatement = getCallableStatement();
            return getDeclaredMethod(callableStatement, "executeQuery");
        }
    }



    public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(clazz.getName() + "." + name + Arrays.toString(parameterTypes), e);
        }
    }
}
