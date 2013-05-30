package com.nhn.hippo.testweb.service;

import com.nhn.hippo.testweb.repository.OracleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
@Transactional("oracleTransactionManager")
public class OracleServiceImpl implements OracleService {
    @Autowired
    private OracleDao oracleDao;

    @Override
    public int selectOne() {
        return oracleDao.selectOne();
    }

    @Override
    public void oracleStatement() {
        oracleDao.oracleStatement();
    }
}
