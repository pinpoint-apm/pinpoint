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
package com.navercorp.pinpoint.flink.process;

import com.navercorp.pinpoint.common.server.bo.stat.join.*;
import com.navercorp.pinpoint.flink.Bootstrap;
import com.navercorp.pinpoint.flink.function.ApplicationStatBoWindow;
import com.navercorp.pinpoint.flink.mapper.thrift.stat.JoinAgentStatBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStatBatch;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class TbaseFlatMapper extends RichFlatMapFunction<TBase, Tuple3<String, JoinStatBo, Long>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private transient JoinAgentStatBoMapper joinAgentStatBoMapper;
    private transient ApplicationCache applicationCache;

    public TbaseFlatMapper() {
    }

    public TbaseFlatMapper(JoinAgentStatBoMapper joinAgentStatBoMapper, ApplicationCache applicationCache) {
        this.joinAgentStatBoMapper = joinAgentStatBoMapper;
        this.applicationCache = applicationCache;
    }

    public void open(Configuration parameters) throws Exception {
        this.joinAgentStatBoMapper = new JoinAgentStatBoMapper();
        applicationCache = Bootstrap.getInstance().getApplicationCache();
    }

    @Override
    public void flatMap(TBase tBase, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
        if (tBase instanceof TFAgentStatBatch) {
            if (logger.isDebugEnabled()) {
                logger.debug("raw data : {}", tBase);
            }
            final TFAgentStatBatch tFAgentStatBatch = (TFAgentStatBatch) tBase;
            final JoinAgentStatBo joinAgentStatBo;
            try {
                joinAgentStatBo = joinAgentStatBoMapper.map(tFAgentStatBatch);

                if (joinAgentStatBo == JoinAgentStatBo.EMPTY_JOIN_AGENT_STAT_BO) {
                    return;
                }
            } catch (Exception e) {
                logger.error("can't create joinAgentStatBo object {}", tFAgentStatBatch, e);
                return;
            }

            out.collect(new Tuple3<String, JoinStatBo, Long>(joinAgentStatBo.getId(), joinAgentStatBo, joinAgentStatBo.getTimestamp()));

            final ApplicationCache.ApplicationKey applicationKey = new ApplicationCache.ApplicationKey(joinAgentStatBo.getId(), joinAgentStatBo.getAgentStartTimestamp());
            final String applicationId = applicationCache.findApplicationId(applicationKey);

            if (applicationId.equals(ApplicationCache.NOT_FOUND_APP_ID)) {
                logger.warn("can't found application id");
                return;
            }

            List<JoinApplicationStatBo> joinApplicationStatBoList = JoinApplicationStatBo.createJoinApplicationStatBo(applicationId, joinAgentStatBo, ApplicationStatBoWindow.WINDOW_SIZE);

            for (JoinApplicationStatBo joinApplicationStatBo : joinApplicationStatBoList) {
                out.collect(new Tuple3<String, JoinStatBo, Long>(applicationId, joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
            }
        }
    }
}
