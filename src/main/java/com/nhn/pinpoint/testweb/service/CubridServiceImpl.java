package com.nhn.pinpoint.testweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nhn.pinpoint.testweb.repository.CubridDao;

/**
 *
 */
@Service
@Transactional("cubridTransactionManager")
public class CubridServiceImpl implements CubridService {
	@Autowired
	private CubridDao cubridDao;

	@Override
	public int selectOne() {
		return cubridDao.selectOne();
	}

	@Override
	public void createStatement() {
		cubridDao.createStatement();
	}
	
	@Override
	public void createErrorStatement() {
		cubridDao.createErrorStatement();
	}
}
