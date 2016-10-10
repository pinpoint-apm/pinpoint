/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.testapp.service.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author barney
 *
 */
@Component
public class SqliteService {
    String JDBC_PREFIX = "jdbc:sqlite:";

    String SQL_CREATE_TABLE = "create table person (id integer, name string)";
    String SQL_SELECT = "select * from person";
    String PSTMT_INSERT = "insert into person values(?, ?)";

    private Connection connection;

    private Statement statement;

    private PreparedStatement preparedSatement;

    private ResultSet rs;

    public Map<Integer, String> executeStmt() throws Exception {
        Map<Integer, String> result = new HashMap<Integer, String>();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(JDBC_PREFIX);
            connection.setAutoCommit(false);

            statement = connection.createStatement();

            statement.execute(SQL_CREATE_TABLE);
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");

            connection.commit();

            rs = statement.executeQuery(SQL_SELECT);
            while(rs.next()) {
                result.put(rs.getInt("id"), rs.getString("name"));
            }
        } finally {
            closeResultSet();
            closeStatement();
            closeConnection();
        }
        return result;
    }

    public Map<Integer, String> executePstmt() throws Exception {
        Map<Integer, String> result = new HashMap<Integer, String>();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(JDBC_PREFIX);
            connection.setAutoCommit(false);

            preparedSatement =  connection.prepareStatement(SQL_CREATE_TABLE);
            preparedSatement.execute();
            preparedSatement.close();

            preparedSatement =  connection.prepareStatement(PSTMT_INSERT);
            preparedSatement.setInt(1, 1);
            preparedSatement.setString(2, "leo");
            preparedSatement.executeUpdate();

            preparedSatement.setInt(1, 2);
            preparedSatement.setString(2, "yui");
            preparedSatement.executeUpdate();
            preparedSatement.close();

            connection.commit();

            preparedSatement =  connection.prepareStatement(SQL_SELECT);
            rs = preparedSatement.executeQuery();
            while(rs.next()) {
                result.put(rs.getInt("id"), rs.getString("name"));
            }
        } finally {
            closeResultSet();
            closePreparedSatement();
            closeConnection();
        }
        return result;
    }

    public Map<Integer, String> rollback() throws Exception {
        Map<Integer, String> result = new HashMap<Integer, String>();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(JDBC_PREFIX);
            connection.setAutoCommit(false);

            preparedSatement =  connection.prepareStatement(SQL_CREATE_TABLE);
            preparedSatement.execute();
            preparedSatement.close();

            connection.commit();

            preparedSatement =  connection.prepareStatement(PSTMT_INSERT);
            preparedSatement.setInt(1, 1);
            preparedSatement.setString(2, "leo");
            preparedSatement.executeUpdate();

            preparedSatement.setInt(1, 2);
            preparedSatement.setString(2, "yui");
            preparedSatement.executeUpdate();
            preparedSatement.close();

            connection.rollback();

            preparedSatement =  connection.prepareStatement(SQL_SELECT);
            rs = preparedSatement.executeQuery();
            while(rs.next()) {
                result.put(rs.getInt("id"), rs.getString("name"));
            }
        } finally {
            closeResultSet();
            closePreparedSatement();
            closeConnection();
        }
        return result;
    }

    private void closeResultSet() throws SQLException {
        if(rs != null) {
            rs.close();
        }
    }

    private void closePreparedSatement() throws SQLException {
        if(preparedSatement != null) {
            preparedSatement.close();
        }
    }

    private void closeStatement() throws SQLException {
        if(statement != null) {
            statement.close();
        }
    }

    private void closeConnection() throws SQLException {
        if(connection != null) {
            connection.close();
        }
    }
}
