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
package com.navercorp.pinpoint.flink;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.flink.function.ApplicationStatBoWindow;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.Duration;

/**
 * @author minwoo.jung
 * @author youngjin.kim2
 */
public class StatStreamingVer2Job implements Serializable {
    private static final Logger logger = LogManager.getLogger(StatStreamingVer2Job.class);

    private static final String JOB_NAME = "Aggregation Stat Data";

    public static void main(String[] args) throws Exception {
        final ParameterTool paramTool = ParameterTool.fromArgs(args);
        new StatStreamingVer2Job().start(paramTool);
    }

    public void start(ParameterTool paramTool) throws Exception {
        logger.info("Starting \"{}\" job with job parameter: {}", JOB_NAME, paramTool.toMap());
        final Bootstrap bootstrap = Bootstrap.getInstance(paramTool.toMap());
        final FlowParameters flowParams = new FlowParameters(paramTool);
        final StreamExecutionEnvironment env = bootstrap.createStreamExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(paramTool);
        env
                .addSource(bootstrap.getTcpSourceFunction()).name("TcpSourceFunction")
                .flatMap(bootstrap.getTbaseFlatMapper()).name("TBaseFlatMapper")
                .filter(el -> el.f1 instanceof JoinApplicationStatBo).name("OnlyJoinApplicationStatBo")
                .assignTimestampsAndWatermarks(WatermarkStrategy
                        .<Tuple3<String, JoinStatBo, Long>>forBoundedOutOfOrderness(flowParams.getOutOfOrderness())
                        .withIdleness(flowParams.getIdleness())
                        .withTimestampAssigner((el, t) -> el.f2))
                .keyBy(el -> el.f0)
                .window(TumblingEventTimeWindows.of(flowParams.getWindowSize()))
                .allowedLateness(flowParams.getAllowedLateness())
                .apply(new ApplicationStatBoWindow()).name("ApplicationStatBoWindow")
                .addSink(bootstrap.getStatisticsDao()).name("StatisticsDao");
        env.execute(JOB_NAME);
    }

    private static class FlowParameters {

        private static final int DEFAULT_OUT_OF_ORDERNESS_MILLIS = 0;
        private static final int DEFAULT_IDLENESS_MILLIS = 10000;
        private static final int DEFAULT_WINDOW_SIZE_MILLIS = ApplicationStatBoWindow.WINDOW_SIZE;
        private static final int DEFAULT_ALLOWED_LATENESS_MILLIS = ApplicationStatBoWindow.ALLOWED_LATENESS;

        private static final String PARAM_PREFIX = "pinpoint.flink.";
        private static final String PARAM_OUT_OF_ORDERNESS = PARAM_PREFIX + "outOfOrdernessMillis";
        private static final String PARAM_IDLENESS = PARAM_PREFIX + "idlenessMillis";
        private static final String PARAM_WINDOW_SIZE = PARAM_PREFIX + "windowSizeMillis";
        private static final String PARAM_ALLOWED_LATENESS = PARAM_PREFIX + "allowedLatenessMillis";

        private final int outOfOrdernessMillis;
        private final int idlenessMillis;
        private final int windowSizeMillis;
        private final int allowedLatenessMillis;

        FlowParameters(ParameterTool params) {
            this.outOfOrdernessMillis = params.getInt(PARAM_OUT_OF_ORDERNESS, DEFAULT_OUT_OF_ORDERNESS_MILLIS);
            this.idlenessMillis = params.getInt(PARAM_IDLENESS, DEFAULT_IDLENESS_MILLIS);
            this.windowSizeMillis = params.getInt(PARAM_WINDOW_SIZE, DEFAULT_WINDOW_SIZE_MILLIS);
            this.allowedLatenessMillis = params.getInt(PARAM_ALLOWED_LATENESS, DEFAULT_ALLOWED_LATENESS_MILLIS);
        }

        Duration getOutOfOrderness() {
            return Duration.ofMillis(outOfOrdernessMillis);
        }

        Duration getIdleness() {
            return Duration.ofMillis(idlenessMillis);
        }

        Time getWindowSize() {
            return Time.milliseconds(windowSizeMillis);
        }

        Time getAllowedLateness() {
            return Time.milliseconds(allowedLatenessMillis);
        }

    }

}
