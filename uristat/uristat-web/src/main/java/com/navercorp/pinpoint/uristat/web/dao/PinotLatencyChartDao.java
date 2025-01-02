package com.navercorp.pinpoint.uristat.web.dao;

import com.navercorp.pinpoint.uristat.web.entity.LatencyChartEntity;
import com.navercorp.pinpoint.uristat.web.mapper.EntityToModelMapper;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotLatencyChartDao implements UriStatChartDao {
    private static final String NAMESPACE = UriStatChartDao.class.getName() + ".";
    private static final String SELECT_LATENCY_CHART = "selectUriLatency";

    private final SqlSessionTemplate sqlPinotSessionTemplate;
    private final EntityToModelMapper mapper;

    public PinotLatencyChartDao(
            @Qualifier("uriStatPinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate,
            EntityToModelMapper mapper
    ) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public List<UriStatChartValue> getChartData(UriStatChartQueryParameter queryParameter) {
        List<LatencyChartEntity> entities = sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_LATENCY_CHART, queryParameter);
        return entities.stream()
                .map(mapper::toModel
                ).toList();
    }

}
