package com.nhn.pinpoint.testweb.service;

import com.nhn.pinpoint.testweb.repository.OracleDao;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void createStatement() {
        oracleDao.createStatement();
    }
}
