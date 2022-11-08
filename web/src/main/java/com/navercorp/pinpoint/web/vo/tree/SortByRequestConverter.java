package com.navercorp.pinpoint.web.vo.tree;

import org.springframework.core.convert.converter.Converter;

/**
 * @author intr3p1d
 */
public class SortByRequestConverter implements Converter<String, SortByAgentInfo.Rules> {
    @Override
    public SortByAgentInfo.Rules convert(String sortBy) {
        return SortByAgentInfo.of(sortBy);
    }
}
