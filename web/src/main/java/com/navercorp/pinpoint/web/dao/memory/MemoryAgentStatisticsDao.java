package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Taejin Koo
 */
@Repository
public class MemoryAgentStatisticsDao implements AgentStatisticsDao {

    private Map<Long, Integer> agentCountPerTime = new TreeMap<>(new LongComparator());

    @Override
    public boolean insertAgentCount(AgentCountStatistics agentCountStatistics) {
        agentCountPerTime.put(agentCountStatistics.getTimestamp(), agentCountStatistics.getAgentCount());
        return true;
    }

    @Override
    public List<AgentCountStatistics> selectAgentCount(Range range) {
        Long to = range.getTo();
        long from = range.getFrom();

        List<AgentCountStatistics> result = new ArrayList<>();

        Iterator<Map.Entry<Long, Integer>> iterator = agentCountPerTime.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> next = iterator.next();

            Long key = next.getKey();
            if (key > to) {
                continue;
            }
            if (key < from) {
                break;
            }

            result.add(new AgentCountStatistics(next.getValue(), key));
        }

        return result;
    }

    private static class LongComparator implements Comparator<Long> {

        @Override
        public int compare(Long o1, Long o2) {
            int compare = Long.compare(o2, o1);

            // if same then overwrite.
            return compare;
        }
    }

}
