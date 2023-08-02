package com.navercorp.pinpoint.web.query.service;

import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlNormalizer;
import com.navercorp.pinpoint.common.profiler.sql.OutputParameterParser;
import com.navercorp.pinpoint.common.profiler.sql.SqlNormalizer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SqlQueryService implements QueryService {

    private final SqlNormalizer sqlNormalizer = new DefaultSqlNormalizer();
    private final OutputParameterParser parameterParser = new OutputParameterParser();

    @Override
    public String bind(String metaData, String bind) {
        List<String> bindValues = parameterParser.parseOutputParameter(bind);
        return sqlNormalizer.combineBindValues(metaData, bindValues);
    }

    @Override
    public BindType getBindType() {
        return BindType.SQL;
    }
}
