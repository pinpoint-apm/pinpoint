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

/**
 * @author minwoo.jung
 */

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.flink.dao.hbase.StatisticsDao;
import com.navercorp.pinpoint.flink.function.ApplicationStatBoWindow;
import com.navercorp.pinpoint.flink.function.Timestamp;
import com.navercorp.pinpoint.flink.function.ApplicationStatBoFliter;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatStreamingVer2Job implements Serializable {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        new StatStreamingVer2Job().start();
    }

    public void start() throws Exception {
        logger.info("start job");
        final Bootstrap bootstrap = Bootstrap.getInstance();

        // set data source
        final TcpSourceFunction tcpSourceFunction = bootstrap.getTcpSourceFuncation();
        final StreamExecutionEnvironment env = bootstrap.createStreamExecutionEnvironment();
//        env.setParallelism(1);
        DataStreamSource<TBase> rawData = env.addSource(tcpSourceFunction);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        bootstrap.setSourceFunctionParallel(rawData);

        //0. generation rawdata
        final SingleOutputStreamOperator<Tuple3<String, JoinStatBo, Long>> statOperator = rawData.flatMap(bootstrap.getTbaseFlatMapper());

        //1-1 save data processing application stat raw data
        final StatisticsDao statisticsDao = bootstrap.getStatisticsDao();
        DataStream<Tuple3<String, JoinStatBo, Long>> applicationStatAggregationData = statOperator.filter(new ApplicationStatBoFliter())
            .assignTimestampsAndWatermarks(new Timestamp())
            .keyBy(0)
            .window(TumblingEventTimeWindows.of(Time.milliseconds(ApplicationStatBoWindow.WINDOW_SIZE)))
            .allowedLateness(Time.milliseconds(ApplicationStatBoWindow.ALLOWED_LATENESS))
            .apply(new ApplicationStatBoWindow());
        applicationStatAggregationData.writeUsingOutputFormat(statisticsDao);

        // 1-2. aggregate application stat data
//        statOperator.filter(new FilterFunction<Tuple3<String, JoinStatBo, Long>>() {
//            @Override
//            public boolean filter(Tuple3<String, JoinStatBo, Long> value) throws Exception {
//                if (value.f1 instanceof JoinApplicationStatBo) {
//                    logger.info("1-2 application stat aggre window function : " + value.f1);
//                    return true;
//                }
//
//                return false;
//            }
//            })
//            .assignTimestampsAndWatermarks(new Timestamp())
//            .keyBy(0)
//            .window(TumblingEventTimeWindows.of(Time.seconds(120)))
//            .apply(new WindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow>() {
//                @Override
//                public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
//                    try {
//                        JoinApplicationStatBo joinApplicationStatBo = join(values);
//                        logger.info("1-2 application stat aggre window function : " + joinApplicationStatBo);
//                        out.collect(new Tuple3<>(joinApplicationStatBo.getId(), joinApplicationStatBo, joinApplicationStatBo.getTimestamp()));
//                    } catch (Exception e) {
//                        logger.error("window function error", e);
//                    }
//                }
//
//                private JoinApplicationStatBo join(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
//                    List<JoinApplicationStatBo> joinApplicaitonStatBoList = new ArrayList<JoinApplicationStatBo>();
//                    for (Tuple3<String, JoinStatBo, Long> value : values) {
//                        joinApplicaitonStatBoList.add((JoinApplicationStatBo) value.f1);
//                    }
//                    return JoinApplicationStatBo.joinApplicationStatBo(joinApplicaitonStatBoList);
//
//                }
//            }).writeUsingOutputFormat(statisticsDao);


        // 2. agrregage agent stat
//        statOperator.filter(new FilterFunction<Tuple3<String, JoinStatBo, Long>>() {
//                @Override
//                public boolean filter(Tuple3<String, JoinStatBo, Long> value) throws Exception {
//                    if (value.f1 instanceof JoinAgentStatBo) {
//                        logger.info("2 application stat aggre window function : " + value.f1);
//                        return true;
//                    }
//
//                    return false;
//                }
//            })
//            .assignTimestampsAndWatermarks(new Timestamp())
//            .keyBy(0)
//            .window(TumblingEventTimeWindows.of(Time.seconds(120)))
//
//            .apply(new WindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow>() {
//
//                @Override
//                public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
//                    try {
//                        JoinAgentStatBo joinAgentStatBo = join(values);
//                        logger.info("2 agent stat aggre window function : " + joinAgentStatBo);
//                        out.collect(new Tuple3<>(joinAgentStatBo.getId(), joinAgentStatBo, joinAgentStatBo.getTimestamp()));
//                    } catch (Exception e) {
//                        logger.error("window function error", e);
//                    }
//                }
//
//                private JoinAgentStatBo join(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
//                    List<JoinAgentStatBo> joinAgentStatBoList =  new ArrayList<JoinAgentStatBo>();
//                    for (Tuple3<String, JoinStatBo, Long> value : values) {
//                        joinAgentStatBoList.add((JoinAgentStatBo) value.f1);
//                    }
//
//                    return JoinAgentStatBo.joinAgentStatBo(joinAgentStatBoList);
//                }
//            })
//            .writeUsingOutputFormat(statisticsDao);

        env.execute("Aggregation Stat Data");
    }
}
