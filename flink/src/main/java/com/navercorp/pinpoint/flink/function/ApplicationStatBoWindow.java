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
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
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
public class ApplicationStatBoWindow implements WindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int WINDOW_SIZE = 10000;
    public static final int ALLOWED_LATENESS = 45000;

    @Override
    public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
        try {
            JoinApplicationStatBo joinApplicationStatBo = join(values);
            long delayTime = new Date().getTime() - joinApplicationStatBo.getTimestamp();
            if (delayTime > 35000) {
                logger.info("[join][delay3]" + new Date(joinApplicationStatBo.getTimestamp()) + " : " +joinApplicationStatBo);
            } else if (delayTime > 25000) {
                logger.info("[join][delay2]" + new Date(joinApplicationStatBo.getTimestamp()) + " : " +joinApplicationStatBo);
            } else if (delayTime > 15000) {
                logger.info("[join][delay1]" + new Date(joinApplicationStatBo.getTimestamp()) + " : " +joinApplicationStatBo);
            } else {
                logger.info("[join][non] " + new Date(joinApplicationStatBo.getTimestamp()) + " : " +joinApplicationStatBo);
            }

            if (joinApplicationStatBo == JoinApplicationStatBo.EMPTY_JOIN_APPLICATION_STAT_BO) {
                return;
            }

            out.collect(new Tuple3<>(joinApplicationStatBo.getId(), joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
        } catch (Exception e) {
            logger.error("window function error", e);
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
