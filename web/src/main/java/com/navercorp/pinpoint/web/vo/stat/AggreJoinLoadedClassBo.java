/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;

public class AggreJoinLoadedClassBo extends JoinLoadedClassBo implements AggregationStatData {

    public AggreJoinLoadedClassBo() {
    }

    public AggreJoinLoadedClassBo(String id, long avgLoadedClass, long maxLoadedClass, String maxLoadedClassAgentId, long minLoadedClass, String minLoadedClassAgentId,
                                  long avgUnloadedClass, long maxUnloadedClass, String maxUnloadedClassAgentId, long minUnloadedClass, String minUnloadedClassAgentId,
                                  long timestamp) {
        super(id, avgLoadedClass, maxLoadedClass, maxLoadedClassAgentId, minLoadedClass, minLoadedClassAgentId, avgUnloadedClass, maxUnloadedClass,
                maxUnloadedClassAgentId, minUnloadedClass, minUnloadedClassAgentId, timestamp);

    }

    public AggreJoinLoadedClassBo(String id, JoinLongFieldBo loadedClass, JoinLongFieldBo unloadedClass, long timestamp) {
        super(id, loadedClass, unloadedClass, timestamp);
    }

    public static AggreJoinLoadedClassBo createUncollectedObject(long timestamp) {
        AggreJoinLoadedClassBo aggreJoinLoadedClassBo = new AggreJoinLoadedClassBo();
        aggreJoinLoadedClassBo.setTimestamp(timestamp);
        return aggreJoinLoadedClassBo;
    }
}
