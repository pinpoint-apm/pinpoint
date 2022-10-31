package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.springframework.core.convert.converter.Converter;

import java.util.Comparator;

/**
 * @author intr3p1d
 */
public class SortByRequestConverter implements Converter<String, SortByAgentInfo.Rules> {
    @Override
    public SortByAgentInfo.Rules convert(String sortBy) {
        return SortByAgentInfo.of(sortBy);
    }
}
