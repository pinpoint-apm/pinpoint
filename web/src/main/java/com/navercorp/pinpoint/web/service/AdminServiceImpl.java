package com.nhn.pinpoint.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.web.dao.ApplicationIndexDao;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	ApplicationIndexDao applicationIndexDao;

	@Override
	public void removeApplicationName(String applicationName) {
		applicationIndexDao.deleteApplicationName(applicationName);
	}

    @Override
    public void removeAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
    }
	
}
