package com.nhn.hippo.testweb.repository;

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
public class OracleDaoIbatis implements OracleDao {

    @Autowired
    @Qualifier("oracleSqlMapClientTemplate")
    private SqlMapClientTemplate sqlMapClientTemplate;

    @Autowired
    @Qualifier("oracleDatasource")
    private DataSource oracleDatasource;

    @Override
    public int selectOne() {
        return (Integer) sqlMapClientTemplate.queryForObject("selectOne");
    }

    @Override
    public void oracleStatement() {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = oracleDatasource.getConnection();
            statement = connection.createStatement();
            statement.execute("select 1 from dual");
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
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
