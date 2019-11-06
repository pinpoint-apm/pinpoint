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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class JoinAgentStatBoFunction implements WindowFunction<Tuple3<String, JoinStatBo, Long>, Tuple3<String, JoinStatBo, Long>, Tuple, TimeWindow> {
    @Override
    public void apply(Tuple tuple, TimeWindow window, Iterable<Tuple3<String, JoinStatBo, Long>> values, Collector<Tuple3<String, JoinStatBo, Long>> out) throws Exception {
        JoinAgentStatBo joinAgentStatBo = join(values);
        out.collect(new Tuple3<>(joinAgentStatBo.getId(), joinAgentStatBo, joinAgentStatBo.getTimestamp()));
    }

    private JoinAgentStatBo join(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
        List<JoinAgentStatBo> joinAgentStatBoList =  new ArrayList<JoinAgentStatBo>();
        for (Tuple3<String, JoinStatBo, Long> value : values) {
            joinAgentStatBoList.add((JoinAgentStatBo) value.f1);
        }

        return JoinAgentStatBo.joinAgentStatBo(joinAgentStatBoList);
    }
}
