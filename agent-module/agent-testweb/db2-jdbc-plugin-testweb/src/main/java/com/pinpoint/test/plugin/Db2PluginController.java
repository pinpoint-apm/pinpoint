/*
 * Copyright 2022 NAVER Corp.
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

package com.pinpoint.test.plugin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

@RestController
public class Db2PluginController {

    public static final String DB_NAME = "test";
    public static final String URL = "jdbc:db2://localhost:32771/test";

    static final String PREPARED_STATEMENT_QUERY = "SELECT * FROM EMPLS WHERE Dept_ID = ?";
    static final String PROCEDURE_NAME = "getEmployeeByName";
    static final String PROCEDURE_DDL =
            "CREATE OR REPLACE PROCEDURE " + PROCEDURE_NAME
                    + "(IN p_name VARCHAR(20), OUT outputParamCount INTEGER) "
                    + "LANGUAGE SQL "
                    + "BEGIN "
                    + "SET outputParamCount = (SELECT COUNT(*) FROM EMPLS WHERE E_Name = p_name); "
                    + "END";
    static final String CALLABLE_STATEMENT_QUERY = "{ CALL " + PROCEDURE_NAME + "(?, ?) }";
    static final String CALLABLE_STATEMENT_INPUT_PARAM = "Tom";
    static final int CALLABLE_STATEMENT_OUTPUT_PARAM_TYPE = Types.INTEGER;

    @RequestMapping(value = "/db2/execute1")
    public String execute1() throws Exception {
        Connection connection = getConnection();

        ResultSet resultSet = connection.createStatement().executeQuery("select * from EMPLS");

        StringBuilder sb = new StringBuilder();
        while(resultSet.next()) {
            sb.append(resultSet);
        }

        connection.close();

        return sb.toString();
    }

    @RequestMapping(value = "/db2/execute2")
    public String execute2() throws Exception {
        executePreparedStatement();
        return "OK";
    }

    @RequestMapping(value = "/db2/execute3")
    public String execute3() throws Exception {
        executeCallableStatement();
        return "OK";
    }

    private int executePreparedStatement() throws Exception {
        int resultCount = 0;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(PREPARED_STATEMENT_QUERY)) {
            ps.setInt(1, 20);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++resultCount;
                }
            }
        }
        return resultCount;
    }

    private int executeCallableStatement() throws Exception {
        try (Connection connection = getConnection()) {
            ensureProcedure(connection);
            try (CallableStatement cs = connection.prepareCall(CALLABLE_STATEMENT_QUERY)) {
                cs.setString(1, CALLABLE_STATEMENT_INPUT_PARAM);
                cs.registerOutParameter(2, CALLABLE_STATEMENT_OUTPUT_PARAM_TYPE);
                cs.execute();
                return cs.getInt(2);
            }
        }
    }

    private void ensureProcedure(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(PROCEDURE_DDL);
        }
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.ibm.db2.jcc.DB2Driver");
        return DriverManager.getConnection(URL, Db2PluginConstants.USERNAME, Db2PluginConstants.PASSWORD);
    }
}