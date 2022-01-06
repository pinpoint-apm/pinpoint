package com.navercorp.pinpoint.web.query;

import com.navercorp.pinpoint.web.util.MongoJsonParser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoQueryService implements QueryService {

    private final MongoJsonParser mongoJsonParser = new DefaultMongoJsonParser();
    private final OutputParameterMongoJsonParser parameterJsonParser = new OutputParameterMongoJsonParser();

    @Override
    public String bind(String metaData, String bind) {
        List<String> bindValues = parameterJsonParser.parseOutputParameter(bind);
        return mongoJsonParser.combineBindValues(metaData, bindValues);
    }

    @Override
    public BindType getBindType() {
        return BindType.MONGO_JSON;
    }
}
