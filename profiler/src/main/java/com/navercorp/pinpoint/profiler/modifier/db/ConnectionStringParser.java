package com.navercorp.pinpoint.profiler.modifier.db;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * @author emeroad
 */
public interface ConnectionStringParser {
    DatabaseInfo parse(String url);
}
