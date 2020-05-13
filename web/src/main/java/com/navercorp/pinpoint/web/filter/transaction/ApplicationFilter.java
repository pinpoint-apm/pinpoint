package com.navercorp.pinpoint.web.filter.transaction;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.URLPatternFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class ApplicationFilter implements Filter<NodeContext> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Filter<SpanBo> spanResponseConditionFilter;
    private final URLPatternFilter acceptURLFilter;

    public ApplicationFilter(Filter<SpanBo> spanResponseConditionFilter, URLPatternFilter acceptURLFilter) {
        this.spanResponseConditionFilter = Objects.requireNonNull(spanResponseConditionFilter, "spanResponseConditionFilter");
        this.acceptURLFilter = Objects.requireNonNull(acceptURLFilter, "acceptURLFilter");
    }

    @Override
    public boolean include(NodeContext nodeContext) {
        final List<SpanBo> spanList = nodeContext.findApplicationNode();
        if (spanList.isEmpty()) {
            logger.debug("Find no application node, nodeContext:{}", nodeContext);
            return false;
        }

        if (!acceptURLFilter.accept(spanList)) {
            return false;
        }

        return responseFilter(spanList);
    }

    private boolean responseFilter(List<SpanBo> spanList) {
        for (SpanBo span : spanList) {
            if (this.spanResponseConditionFilter.include(span)) {
                return true;
            }
        }
        return false;
    }
}
