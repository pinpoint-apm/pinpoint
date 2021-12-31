package com.navercorp.pinpoint.web.query;

import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlParser;
import com.navercorp.pinpoint.common.profiler.sql.OutputParameterParser;
import com.navercorp.pinpoint.common.profiler.sql.SqlParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SqlQueryService implements QueryService {

    private final SqlParser sqlParser = new DefaultSqlParser();
    private final OutputParameterParser parameterParser = new OutputParameterParser();

    @Override
    public String bind(String metaData, String bind) {
        List<String> bindValues = parameterParser.parseOutputParameter(bind);
        return sqlParser.combineBindValues(metaData, bindValues);
    }

    @Override
    public BindType getBindType() {
        return BindType.SQL;
    }
}
