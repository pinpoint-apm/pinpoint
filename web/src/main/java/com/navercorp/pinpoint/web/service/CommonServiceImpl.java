package com.nhn.pinpoint.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.vo.Application;

/**
 * @author netspider
 */
@Service
public class CommonServiceImpl implements CommonService {

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Override
	public List<Application> selectAllApplicationNames() {
		return applicationIndexDao.selectAllApplicationNames();
	}
}
