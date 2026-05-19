package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceSpanEventMapperTest {

    // None of the tests below exercise DB type resolution, so the registry can be empty —
    // resolver gracefully falls back to OPENTELEMETRY_DB.
    private static final ServiceTypeRegistryService EMPTY_REGISTRY = new ServiceTypeRegistryService() {
        @Override
        public ServiceType findServiceType(int serviceType) {
            return ServiceType.UNDEFINED;
        }

        @Override
        public ServiceType findServiceTypeByName(String typeName) {
            return ServiceType.UNDEFINED;
        }

        @Override
        public List<ServiceType> findDesc(String desc) {
            return List.of();
        }
    };

    private OtlpTraceSpanEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceSpanEventMapper(
                new OtlpTraceEventMapper(new ObjectMapper()),
                EMPTY_REGISTRY,
                new OtlpMessagingTypeResolver(EMPTY_REGISTRY));
    }

    // =======================================================================
    // getDbSystem
    // =======================================================================

    @Test
    void getDbSystem_dbSystemName_preferred() {
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM_NAME, AttributeValue.of("postgresql"));
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("mysql"));

        assertThat(mapper.getDbSystem(attrs)).isEqualTo("postgresql");
    }

    @Test
    void getDbSystem_fallbackToDbSystem_whenDbSystemNameAbsent() {
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("mysql"));

        assertThat(mapper.getDbSystem(attrs)).isEqualTo("mysql");
    }

    @Test
    void getDbSystem_returnsNull_whenBothAbsent() {
        assertThat(mapper.getDbSystem(Map.of())).isNull();
    }

    // =======================================================================
    // isDatabase
    // =======================================================================

    @Test
    void isDatabase_true_whenDbSystemPresent() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("mysql")
        );
        assertThat(mapper.isDatabase(attrs)).isTrue();
    }

    @Test
    void isDatabase_true_whenDbSystemNamePresent() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM_NAME, AttributeValue.of("postgresql")
        );
        assertThat(mapper.isDatabase(attrs)).isTrue();
    }

    @Test
    void isDatabase_false_whenNeitherPresent() {
        assertThat(mapper.isDatabase(Map.of())).isFalse();
    }

    // =======================================================================
    // isDatabaseExecuteQuery
    // =======================================================================

    @Test
    void isDatabaseExecuteQuery_true_whenDbQueryTextPresent() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_QUERY_TEXT, AttributeValue.of("SELECT 1")
        );
        assertThat(mapper.isDatabaseExecuteQuery(attrs)).isTrue();
    }

    @Test
    void isDatabaseExecuteQuery_true_whenDbStatementPresent() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, AttributeValue.of("SELECT 1")
        );
        assertThat(mapper.isDatabaseExecuteQuery(attrs)).isTrue();
    }

    @Test
    void isDatabaseExecuteQuery_false_whenNeitherPresent() {
        assertThat(mapper.isDatabaseExecuteQuery(Map.of())).isFalse();
    }

    // =======================================================================
    // getClientSpanDbStatement
    // =======================================================================

    @Test
    void getClientSpanDbStatement_prefersDbQueryText() {
        Map<String, AttributeValue> attrs = new HashMap<>();
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_DB_QUERY_TEXT, AttributeValue.of("SELECT 1"));
        attrs.put(OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, AttributeValue.of("SELECT 2"));

        assertThat(mapper.getClientSpanDbStatement(attrs)).isEqualTo("SELECT 1");
    }

    @Test
    void getClientSpanDbStatement_fallbackToDbStatement() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_STATEMENT, AttributeValue.of("SELECT 2")
        );
        assertThat(mapper.getClientSpanDbStatement(attrs)).isEqualTo("SELECT 2");
    }

    @Test
    void getClientSpanDbStatement_returnsNull_whenBothAbsent() {
        assertThat(mapper.getClientSpanDbStatement(Map.of())).isNull();
    }
}
