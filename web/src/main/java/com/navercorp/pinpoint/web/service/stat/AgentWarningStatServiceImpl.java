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

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentWarningStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.dao.stat.DeadlockDao;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentState;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
@Service
public class AgentWarningStatServiceImpl implements AgentWarningStatService {

    private static final long LIMIT_TIME = 60000;

    @Autowired
    @Qualifier("deadlockDaoV2")
    private DeadlockDao deadlockDao;

    @Override
    public List<AgentStatusTimelineSegment> select(String agentId, Range range) {
        List<AgentWarningStatDataPoint> agentWarningStatDataPointList = select0(agentId, range);
        return createTimelineSegment(agentWarningStatDataPointList);
    }

    @Override
    public Map<Long, List<AgentStatusTimelineSegment>> selectSeparatedByStartTimestamp(String agentId, Range range) {
        Map<Long, List<AgentStatusTimelineSegment>> result = new HashMap<>();

        List<AgentWarningStatDataPoint> agentWarningStatDataPointList = select0(agentId, range);

        Map<Long, List<AgentWarningStatDataPoint>> partitions = parseByStartTimestamp(agentWarningStatDataPointList);
        for (Map.Entry<Long, List<AgentWarningStatDataPoint>> entry : partitions.entrySet()) {
            long agentStartTimestamp = entry.getKey();
            List<AgentWarningStatDataPoint> agentLifeCycleEvents = entry.getValue();

            List<AgentStatusTimelineSegment> timelineSegmentList = createTimelineSegment(agentLifeCycleEvents);
            result.put(agentStartTimestamp, timelineSegmentList);
        }

        return result;
    }

    private List<AgentWarningStatDataPoint> select0(String agentId, Range range) {
        List<AgentWarningStatDataPoint> agentWarningStatDataPointList = new ArrayList<>();

        List<DeadlockBo> deadlockBoList = deadlockDao.getAgentStatList(agentId, range);
        for (DeadlockBo deadlockBo : deadlockBoList) {
            agentWarningStatDataPointList.add(deadlockBo);
        }

        return agentWarningStatDataPointList;
    }

    private Map<Long, List<AgentWarningStatDataPoint>> parseByStartTimestamp(List<AgentWarningStatDataPoint> agentWarningStatDataPointList) {
        Map<Long, List<AgentWarningStatDataPoint>> partitions = new HashMap<>();

        if (CollectionUtils.hasLength(agentWarningStatDataPointList)) {
            for (AgentWarningStatDataPoint agentWarningStatDataPoint : agentWarningStatDataPointList) {
                long startTimestamp = agentWarningStatDataPoint.getStartTimestamp();
                List<AgentWarningStatDataPoint> partition = partitions.get(startTimestamp);
                if (partition == null) {
                    partition = new ArrayList<>();
                    partitions.put(startTimestamp, partition);
                }
                partition.add(agentWarningStatDataPoint);
            }
        }
        return partitions;
    }

    private List<AgentStatusTimelineSegment> createTimelineSegment(List<AgentWarningStatDataPoint> agentWarningStatDataPointList) {
        Collections.sort(agentWarningStatDataPointList, new Comparator<AgentWarningStatDataPoint>() {
            @Override
            public int compare(AgentWarningStatDataPoint o1, AgentWarningStatDataPoint o2) {
                int eventTimestampComparison = Long.compare(o1.getTimestamp(), o2.getTimestamp());
                return eventTimestampComparison;
            }
        });

        return createTimelineSegment0(agentWarningStatDataPointList);
    }

    private List<AgentStatusTimelineSegment> createTimelineSegment0(List<AgentWarningStatDataPoint> agentWarningStatDataPointList) {
        if (CollectionUtils.isEmpty(agentWarningStatDataPointList)) {
            return Collections.emptyList();
        }

        List<AgentStatusTimelineSegment> timelineSegmentList = new ArrayList<>();

        long beforeTimestamp = -1;
        int index = 0;
        for (int i = 0; i < agentWarningStatDataPointList.size(); i++) {
            AgentWarningStatDataPoint agentWarningStatDataPoint = agentWarningStatDataPointList.get(i);
            if (i == 0) {
                beforeTimestamp = agentWarningStatDataPoint.getTimestamp();
            } else {
                boolean needSeparation = agentWarningStatDataPoint.getTimestamp() > beforeTimestamp + LIMIT_TIME;
                if (needSeparation) {
                    AgentStatusTimelineSegment timelineSegment = createUnstableTimelineSegment(agentWarningStatDataPointList.subList(index, i));
                    timelineSegmentList.add(timelineSegment);

                    beforeTimestamp = agentWarningStatDataPoint.getTimestamp();
                    index = i;
                }
                beforeTimestamp = agentWarningStatDataPoint.getTimestamp();
            }
        }

        AgentStatusTimelineSegment timelineSegment = createUnstableTimelineSegment(agentWarningStatDataPointList.subList(index, agentWarningStatDataPointList.size()));
        timelineSegmentList.add(timelineSegment);

        return timelineSegmentList;
    }

    private AgentStatusTimelineSegment createUnstableTimelineSegment(List<AgentWarningStatDataPoint> agentWarningStatDataPointList) {
        if (CollectionUtils.hasLength(agentWarningStatDataPointList)) {
            AgentWarningStatDataPoint first = ListUtils.getFirst(agentWarningStatDataPointList);
            AgentWarningStatDataPoint last = ListUtils.getLast(agentWarningStatDataPointList);

            AgentStatusTimelineSegment timelineSegment = new AgentStatusTimelineSegment();
            timelineSegment.setStartTimestamp(first.getTimestamp());
            timelineSegment.setEndTimestamp(last.getTimestamp());
            timelineSegment.setValue(AgentState.UNSTABLE_RUNNING);
            return timelineSegment;
        }

        return null;
    }

}
