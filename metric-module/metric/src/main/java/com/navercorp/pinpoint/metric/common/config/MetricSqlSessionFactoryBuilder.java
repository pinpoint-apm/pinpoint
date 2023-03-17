package com.navercorp.pinpoint.metric.common.config;

import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.mybatis.MetricDataTypeHandler;
import com.navercorp.pinpoint.metric.web.dao.model.HostInfoSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.MetricInfoSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.MetricTagsSearchKey;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.DoubleToLongTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.DoubleTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.LongTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.TagListTypeHandler;
import com.navercorp.pinpoint.metric.web.mybatis.typehandler.TagTypeHandler;
import com.navercorp.pinpoint.metric.web.util.MetricsQueryParameter;
import com.navercorp.pinpoint.metric.web.util.Range;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MetricSqlSessionFactoryBuilder {

    private DataSource dataSource;
    private TransactionFactory transactionFactory;
    private Resource[] mappers;
    private Configuration configuration;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTransactionFactory(TransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
    }

    public void setMappers(Resource[] mappers) {
        this.mappers = mappers;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Bean
    public SqlSessionFactory build() throws Exception {

        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfiguration(configuration);
        sessionFactoryBean.setMapperLocations(mappers);
        sessionFactoryBean.setFailFast(true);
        sessionFactoryBean.setTransactionFactory(transactionFactory);
        return sessionFactoryBean.getObject();
    }

    public void registerCommonTypeAlias() {
        TypeAliasRegistry typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        typeAliasRegistry.registerAlias("Number", Number.class);

        typeAliasRegistry.registerAlias("Tag", Tag.class);
        typeAliasRegistry.registerAlias("DoubleMetric", DoubleMetric.class);
        typeAliasRegistry.registerAlias("MetricData", MetricData.class);
        typeAliasRegistry.registerAlias("MetricDataName", MetricDataName.class);
        typeAliasRegistry.registerAlias("MetricDataType", MetricDataType.class);
        typeAliasRegistry.registerAlias("MetricTag", MetricTag.class);
        typeAliasRegistry.registerAlias("MetricTagKey", MetricTagKey.class);
        typeAliasRegistry.registerAlias("MetricDataTypeHandler", MetricDataTypeHandler.class);
        typeAliasRegistry.registerAlias("TagListTypeHandler", com.navercorp.pinpoint.metric.common.model.mybatis.TagListTypeHandler.class);

    }

    public void registerWebTypeAlias() {
        registerCommonTypeAlias();

        TypeAliasRegistry typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        typeAliasRegistry.registerAlias("Range", Range.class);
        typeAliasRegistry.registerAlias("MetricsQueryParameter", MetricsQueryParameter.class);
        typeAliasRegistry.registerAlias("DoubleHandler", DoubleTypeHandler.class);
        typeAliasRegistry.registerAlias("LongHandler", LongTypeHandler.class);
        typeAliasRegistry.registerAlias("DoubleToLongHandler", DoubleToLongTypeHandler.class);
        typeAliasRegistry.registerAlias("TagHandler", TagTypeHandler.class);
        typeAliasRegistry.registerAlias("TagListHandler", TagListTypeHandler.class);
        typeAliasRegistry.registerAlias("SampledSystemMetric", SampledSystemMetric.class);
        typeAliasRegistry.registerAlias("SystemMetricPoint", SystemMetricPoint.class);
        typeAliasRegistry.registerAlias("systemMetricDataSearchKey", SystemMetricDataSearchKey.class);
        typeAliasRegistry.registerAlias("metricInfoSearchKey", MetricInfoSearchKey.class);
        typeAliasRegistry.registerAlias("metricTagsSearchKey", MetricTagsSearchKey.class);
        typeAliasRegistry.registerAlias("hostInfoSearchKey", HostInfoSearchKey.class);
    }

    public void registerCommonTypeHandler() {
        TypeHandlerRegistry typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
        typeHandlerRegistry.register(MetricDataType.class, MetricDataTypeHandler.class);
        typeHandlerRegistry.register(List.class, com.navercorp.pinpoint.metric.common.model.mybatis.TagListTypeHandler.class);


    }

    public void registerWebTypeHandler() {
        registerCommonTypeHandler();


        TypeHandlerRegistry typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
        //--- web
        typeHandlerRegistry.register(Number.class, DoubleTypeHandler.class);
        typeHandlerRegistry.register(Number.class, LongTypeHandler.class);
        typeHandlerRegistry.register(Number.class, DoubleToLongTypeHandler.class);
        typeHandlerRegistry.register(Tag.class, TagTypeHandler.class);
        typeHandlerRegistry.register(List.class, TagListTypeHandler.class);
    }

}
