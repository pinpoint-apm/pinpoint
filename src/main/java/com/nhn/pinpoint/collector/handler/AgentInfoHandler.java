package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.collector.dao.AgentInfoDao;
import com.nhn.pinpoint.collector.dao.ApplicationIndexDao;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.thrift.dto.TResult;

/**
 * @author emeroad
 * @author koo.taejin
 */
@Service("agentInfoHandler")
public class AgentInfoHandler implements SimpleHandler, RequestResponseHandler {

	private final Logger logger = LoggerFactory.getLogger(AgentInfoHandler.class.getName());

	@Autowired
	private AgentInfoDao agentInfoDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	public void handleSimple(TBase<?, ?> tbase) {
		handleRequest(tbase);
	}
	
	@Override
	public TBase<?, ?> handleRequest(TBase<?, ?> tbase) {
		if (!(tbase instanceof TAgentInfo)) {
			logger.warn("invalid tbase:{}", tbase);
			// 해당 BO뿐만 아니라 다른 BO에서도 null을 반환하는 경우가 있음
			// 이 경우 Req/Res 방식을 경우 상대방에게 응답이 안가는데 이게 정상인지 잘모르겠네 문의 필요.
			return null;
		}

		try {
			TAgentInfo agentInfo = (TAgentInfo) tbase;

			logger.debug("Received AgentInfo={}", agentInfo);

			// agent info
			agentInfoDao.insert(agentInfo);

			// applicationname으로 agentid를 조회하기위한 용도.
			applicationIndexDao.insert(agentInfo);
			
			return new TResult(true);
			// agentid로 applicationname을 조회하기 위한 용도
//			agentIdApplicationIndexDao.insert(agentInfo.getAgentId(), agentInfo.getApplicationName());
		} catch (Exception e) {
			logger.warn("AgentInfo handle error. Caused:{}", e.getMessage(), e);
			TResult result = new TResult(false);
			result.setMessage(e.getMessage());
			return result;
		}
	}
	
}
