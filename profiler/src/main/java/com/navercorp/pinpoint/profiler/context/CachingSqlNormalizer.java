package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 */
public interface CachingSqlNormalizer {
    ParsingResult wrapSql(String sql);

    boolean normalizedSql(ParsingResult sql);
}

