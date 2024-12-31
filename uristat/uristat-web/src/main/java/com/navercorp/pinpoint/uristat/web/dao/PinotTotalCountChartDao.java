package com.navercorp.pinpoint.uristat.web.dao;

import com.navercorp.pinpoint.uristat.web.model.UriStatChartValue;
import com.navercorp.pinpoint.uristat.web.util.UriStatChartQueryParameter;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotTotalCountChartDao implements UriStatChartDao {
    private static final String NAMESPACE = UriStatChartDao.class.getName() + ".";
    private static final String SELECT_TOTAL_CHART = "selectTotalUriStat";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    public PinotTotalCountChartDao(@Qualifier("uriStatPinotSessionTemplate") SqlSessionTemplate sqlPinotSessionTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
    }

    @Override
    public List<UriStatChartValue> getChartData(UriStatChartQueryParameter queryParameter) {
        return sqlPinotSessionTemplate.selectList(NAMESPACE + SELECT_TOTAL_CHART, queryParameter);
    }
}
