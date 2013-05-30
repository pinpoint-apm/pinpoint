package com.nhn.hippo.testweb.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public class OracleDaoIbatis implements OracleDao {

    @Autowired
    @Qualifier("oracleSqlMapClientTemplate")
    private SqlMapClientTemplate sqlMapClientTemplate;

    @Override
    public int selectOne() {
        return (Integer) sqlMapClientTemplate.queryForObject("selectOne");
    }
}
