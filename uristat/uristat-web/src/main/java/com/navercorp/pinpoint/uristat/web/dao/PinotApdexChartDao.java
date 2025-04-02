package com.navercorp.pinpoint.uristat.web.dao;

import com.navercorp.pinpoint.uristat.web.entity.UriApdexChartEntity;
import com.navercorp.pinpoint.uristat.web.mapper.EntityToModelMapper;
import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotApdexChartDao implements UriStatChartDao {
    private static final String NAMESPACE = UriStatChartDao.class.getName() + ".";
    private static final String SELECT_APDEX_CHART = "selectUriApdex";

    private final SqlSessionTemplate sqlPinotSessionTemplate;
    private final EntityToModelMapper mapper;

    public PinotApdexChartDao(
            @Qualifier("uriStatPinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate,
            EntityToModelMapper mapper
    ) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public List<UriStatChartValue> getChartData(UriStatChartQueryParameter queryParameter) {
        List<UriApdexChartEntity> entities = sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_APDEX_CHART, queryParameter);
        return entities.stream()
                .map(mapper::toSimpleApdexChart
                ).toList();
    }
}
