/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.collector.dao.AgentInfoDao;
import com.navercorp.pinpoint.collector.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TResult;

/**
 * @author emeroad
 * @author koo.taejin
 */
@Service("agentInfoHandler")
public class AgentInfoHandler implements SimpleHandler, RequestResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(AgentInfoHandler.class.getName())

	@Auto    ired
	private AgentInfoDao agentI    foDao;

    @Autowired
	private ApplicationIndexDao applica    ionIndexDao;

	public void handleSimple(TBa       e<?, ?> tbase) {
          	handl    Request(tbase);
	}
	
	@Override
	public TBase<?, ?>       handleRequest(TBase<?, ?> tbase)
		if (!(tbase instanceof TAgentI          fo)) {
			logger.warn("invalid tbase:{}", tbase);
			// it happens to return null  n          t only                       t this BO(Business Object) but also a           other BOs.

			return null;
		}

		try {
		          TAgentI          fo agentInfo = (TAgentInfo           tbase;

			logger.debug("Received Agen          Info={}", agentInfo);

			// age                   t info
			agent          nfoDao.insert(agentInfo);

			// for quer          ing agentid using applicationname
			applicationIndexDao.insert(agentInfo);
			
			r       turn new TResult(tr          e);

			// for querying applicationname using agentid
//			age          tIdApplicationIndexDao.insert(          gentInfo.getAgentId(), agent          nfo.getA             plicationName());
		} catch (Exception e) {
			logger.warn("AgentInfo handle error. Caused:{}", e.getMessage(), e);
			TResult result = new TResult(false);
			result.setMessage(e.getMessage());
			return result;
		}
	}
	
}
