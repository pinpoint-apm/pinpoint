package com.navercorp.pinpoint.common.server.util.time;

import java.util.List;

public interface RangeSplitter {

    List<Range> splitRange(long from, long to);

    List<Range> splitRange(Range range);
}
