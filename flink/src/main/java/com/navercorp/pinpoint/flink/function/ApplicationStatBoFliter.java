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
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author minwoo.jung
 */
public class ApplicationStatBoFliter implements FilterFunction<Tuple3<String, JoinStatBo, Long>> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean filter(Tuple3<String, JoinStatBo, Long> value) throws Exception {
        if (value.f1 instanceof JoinApplicationStatBo) {
            logger.info("1-1 " + "(" + new Date(value.f1.getTimestamp()) + ")" + value.f1.toString());
            return true;
        }

        return false;
    }
}
