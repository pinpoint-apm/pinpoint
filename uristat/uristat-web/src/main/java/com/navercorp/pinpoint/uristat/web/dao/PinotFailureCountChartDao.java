package com.navercorp.pinpoint.uristat.web.dao;

import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotFailureCountChartDao implements UriStatChartDao {
    private static final String NAMESPACE = UriStatChartDao.class.getName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    public PinotFailureCountChartDao(@Qualifier("uriStatPinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
    }

    @Override
    public List<UriStatChartValue> getChartDataApplication(UriStatChartQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectFailedUriStatApplication", queryParameter);
    }

    @Override
    public List<UriStatChartValue> getChartDataAgent(UriStatChartQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + "selectFailedUriStatAgentId", queryParameter);
    }
}
