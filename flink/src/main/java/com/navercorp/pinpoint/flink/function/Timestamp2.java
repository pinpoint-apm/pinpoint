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
package com.navercorp.pinpoint.flink.function;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

/**
 * @author minwoo.jung
 */
public class Timestamp2 implements AssignerWithPunctuatedWatermarks<Tuple3<String, JoinStatBo, Long>> {

    private static final long serialVersionUID = 1L;

    @Override
    public Watermark checkAndGetNextWatermark(Tuple3<String, JoinStatBo, Long> lastElement, long extractedTimestamp) {
        return new Watermark(lastElement.f2);
    }

    @Override
    public long extractTimestamp(Tuple3<String, JoinStatBo, Long> value, long previousElementTimestamp) {
        return value.f2;
    }
}
