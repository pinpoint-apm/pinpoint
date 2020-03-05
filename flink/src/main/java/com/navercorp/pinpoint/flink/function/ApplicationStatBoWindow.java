/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.flink.function;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.flink.Bootstrap;
import org.apache.flink.api.common.ExecutionConfig.GlobalJobParameters;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.RichWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class ApplicationStatBoWindow extends RichWindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow> {
    public static final int WINDOW_SIZE = 10000;
    public static final int ALLOWED_LATENESS = 45000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private transient ApplicationStatBoWindowInterceptor applicationStatBoWindowInterceptor;

    @Override
    public void open(Configuration parameters) throws Exception {
        GlobalJobParameters globalJobParameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        applicationStatBoWindowInterceptor = Bootstrap.getInstance(globalJobParameters.toMap()).getApplicationStatBoWindowInterceptor();

    }

    @Override
    public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
        String tupleKey = (String)tuple.getField(0);
        applicationStatBoWindowInterceptor.before(values);
        try {
            JoinApplicationStatBo joinApplicationStatBo = join(values);
            long delayTime = new Date().getTime() - joinApplicationStatBo.getTimestamp();
            if (delayTime > 35000) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[join][delay3] {} : {}", new Date(joinApplicationStatBo.getTimestamp()), joinApplicationStatBo);
                }
            } else if (delayTime > 25000) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[join][delay2] {} : {}", new Date(joinApplicationStatBo.getTimestamp()), joinApplicationStatBo);
                }
            } else if (delayTime > 15000) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[join][delay1] {} : {}", new Date(joinApplicationStatBo.getTimestamp()), joinApplicationStatBo);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[join][non] {} : {}", new Date(joinApplicationStatBo.getTimestamp()), joinApplicationStatBo);
                }
            }

            if (joinApplicationStatBo == JoinApplicationStatBo.EMPTY_JOIN_APPLICATION_STAT_BO) {
                return;
            }

            Tuple3 resultTuple = applicationStatBoWindowInterceptor.middle(new Tuple3<>(tupleKey, joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
            out.collect(resultTuple);
        } catch (Exception e) {
            logger.error("window function error", e);
        } finally {
            applicationStatBoWindowInterceptor.after();
        }
    }

    private JoinApplicationStatBo join(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
        List<JoinApplicationStatBo> joinApplicaitonStatBoList = new ArrayList<JoinApplicationStatBo>();

        for (Tuple3<String, JoinStatBo, Long> value : values) {
            joinApplicaitonStatBoList.add((JoinApplicationStatBo) value.f1);
        }

        return JoinApplicationStatBo.joinApplicationStatBoByTimeSlice(joinApplicaitonStatBoList);

    }
}