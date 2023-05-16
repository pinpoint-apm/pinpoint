/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.flink.function;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * @author minwoo.jung
 */
public class AgentStatTimestampAssigner implements SerializableTimestampAssigner<Tuple3<String, JoinStatBo, Long>> {
    @Override
    public long extractTimestamp(Tuple3<String, JoinStatBo, Long> value, long l) {
        return value.f2;
    }
}
