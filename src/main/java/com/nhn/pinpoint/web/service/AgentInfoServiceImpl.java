package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.nhn.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class AgentInfoServiceImpl implements AgentInfoService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private AgentInfoDao agentInfoDao;
	
	/**
	 * FIXME 인터페이스에 from, to가 있으나 실제로 사용되지 않음. 나중에 agent list snapshot기능이 추가되면
	 * 사용될 것임.
	 */
	@Override
	public SortedMap<String, List<AgentInfoBo>> getApplicationAgentList(String applicationName, Range range) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        String[] agentIdList = applicationIndexDao.selectAgentIds(applicationName);

        if (logger.isDebugEnabled()) {
		    logger.debug("agentIdList={}", Arrays.toString(agentIdList));
        }
		
		if (agentIdList == null || agentIdList.length == 0) {
			logger.debug("agentIdList is empty. applicationName={}, {}", applicationName, range);
			return new TreeMap<String, List<AgentInfoBo>>();
		}
		
		// key = hostname
		// value= list fo agentinfo
		SortedMap<String, List<AgentInfoBo>> result = new TreeMap<String, List<AgentInfoBo>>();

		for (String agentId : agentIdList) {
			List<AgentInfoBo> agentInfoList = agentInfoDao.getAgentInfo(agentId, range);

			if (agentInfoList.isEmpty()) {
				logger.debug("agentinfolist is empty. agentid={}, {}", agentId, range);
				continue;
			}

			// FIXME 지금은 그냥 첫 번재꺼 사용. 여러개 검사?는 나중에 생각해볼 예정.
			AgentInfoBo agentInfo = agentInfoList.get(0);
			String hostname = agentInfo.getHostname();

			if (result.containsKey(hostname)) {
				result.get(hostname).add(agentInfo);
			} else {
				List<AgentInfoBo> list = new ArrayList<AgentInfoBo>();
				list.add(agentInfo);
				result.put(hostname, list);
			}
		}

		for(Entry<String, List<AgentInfoBo>> entry : result.entrySet()) {
			Collections.sort(entry.getValue());
		}

		logger.info("getApplicationAgentList={}", result);
		
		return result;
	}
}
