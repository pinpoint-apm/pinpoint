package com.nhn.pinpoint.testweb.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
@Repository
public class CubridDaoIbatis implements CubridDao {

    @Autowired
    @Qualifier("cubridSqlMapClientTemplate")
    private SqlMapClientTemplate sqlMapClientTemplate;

    @Autowired
    @Qualifier("cubridDataSource")
    private DataSource datasource;

    @Override
    public int selectOne() {
        return (Integer) sqlMapClientTemplate.queryForObject("selectOne");
    }

    @Override
    public boolean createStatement() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = datasource.getConnection();
            statement = connection.createStatement();
            return statement.execute("select 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }

        }
    }
    
    @Override
    public boolean createErrorStatement() {
    	Connection connection = null;
    	Statement statement = null;
    	try {
    		connection = datasource.getConnection();
    		statement = connection.createStatement();
    		return statement.execute("SELECT * FROM NOT_EXISTS_TABLE");
    	} catch (SQLException e) {
    		throw new RuntimeException(e);
    	} finally {
    		if (statement != null) {
    			try {
    				statement.close();
    			} catch (SQLException e) {
    			}
    		}
    		if (connection != null) {
    			try {
    				connection.close();
    			} catch (SQLException e) {
    			}
    		}
    		
    	}
    }
}
