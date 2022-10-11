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
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentState;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineSegment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class AgentWarningStatServiceImpl implements AgentWarningStatService {

    private static final long LIMIT_TIME = 60000;

    private final AgentStatDao<DeadlockThreadCountBo> deadlockDao;

    public AgentWarningStatServiceImpl(AgentStatDao<DeadlockThreadCountBo> deadlockDao) {
        this.deadlockDao = Objects.requireNonNull(deadlockDao, "deadlockDao");
    }

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

        List<DeadlockThreadCountBo> deadlockThreadCountBoList = deadlockDao.getAgentStatList(agentId, range);

        List<AgentWarningStatDataPoint> agentWarningStatDataPointList = new ArrayList<>(deadlockThreadCountBoList);
        return agentWarningStatDataPointList;
    }

    private Map<Long, List<AgentWarningStatDataPoint>> parseByStartTimestamp(List<AgentWarningStatDataPoint> agentWarningStatDataPointList) {
        if (CollectionUtils.isEmpty(agentWarningStatDataPointList)) {
            return Collections.emptyMap();
        }

        Map<Long, List<AgentWarningStatDataPoint>> partitions = new HashMap<>();
        for (AgentWarningStatDataPoint agentWarningStatDataPoint : agentWarningStatDataPointList) {
            long startTimestamp = agentWarningStatDataPoint.getStartTimestamp();
            List<AgentWarningStatDataPoint> partition = partitions.computeIfAbsent(startTimestamp, k -> new ArrayList<>());
            partition.add(agentWarningStatDataPoint);
        }
        return partitions;
    }

    private List<AgentStatusTimelineSegment> createTimelineSegment(List<AgentWarningStatDataPoint> agentWarningStatDataPointList) {
        agentWarningStatDataPointList.sort(Comparator.comparingLong(AgentWarningStatDataPoint::getTimestamp));
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
        if (CollectionUtils.isEmpty(agentWarningStatDataPointList)) {
            return null;
        }

        AgentWarningStatDataPoint first = CollectionUtils.firstElement(agentWarningStatDataPointList);
        AgentWarningStatDataPoint last = CollectionUtils.lastElement(agentWarningStatDataPointList);
        if(first == null || last == null) {
            return null;
        }

        AgentStatusTimelineSegment timelineSegment = new AgentStatusTimelineSegment();
        timelineSegment.setStartTimestamp(first.getTimestamp());
        timelineSegment.setEndTimestamp(last.getTimestamp());
        timelineSegment.setValue(AgentState.UNSTABLE_RUNNING);
        return timelineSegment;
    }

}
