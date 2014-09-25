package com.nhn.pinpoint.testweb.service;

import com.nhn.pinpoint.testweb.repository.MySqlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
@Transactional("mysqlTransactionManager")
public class MySqlServiceImpl implements MySqlService {

    @Autowired
    private MySqlDao mysqlDao;

    @Override
    public int selectOne() {
        return mysqlDao.selectOne();
    }

    @Override
    public void createStatement() {
        mysqlDao.createStatement();
    }
}
