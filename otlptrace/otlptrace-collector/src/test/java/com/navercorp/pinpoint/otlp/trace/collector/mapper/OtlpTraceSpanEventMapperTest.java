package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.message.OtlpMessagingTypeResolver;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceSpanEventMapperTest {

    // Stub registry pre-loaded with the client-side RPC ServiceTypes so client/RPC dispatch
    // tests resolve to the real plugin codes. DB / messaging resolvers fall back gracefully
    // when their plugin names are absent.
    private static final ServiceTypeRegistryService TEST_REGISTRY = buildRegistry();

    private static ServiceTypeRegistryService buildRegistry() {
        Map<String, ServiceType> byName = new HashMap<>();
        byName.put("GRPC",                   ServiceTypeFactory.of(9160, "GRPC",                   "GRPC"));
        byName.put("APACHE_DUBBO_CONSUMER",  ServiceTypeFactory.of(9997, "APACHE_DUBBO_CONSUMER",  "APACHE_DUBBO_CONSUMER"));
        byName.put("ENVOY_EGRESS",           ServiceTypeFactory.of(9302, "ENVOY_EGRESS",           "ENVOY_EGRESS"));
        return new ServiceTypeRegistryService() {
            @Override
            public ServiceType findServiceType(int serviceType) {
                return byName.values().stream()
                        .filter(t -> t.getCode() == serviceType)
                        .findFirst()
                        .orElse(ServiceType.UNDEFINED);
            }

            @Override
            public ServiceType findServiceTypeByName(String typeName) {
                return byName.getOrDefault(typeName, ServiceType.UNDEFINED);
            }

            @Override
            public List<ServiceType> findDesc(String desc) {
                return List.of();
            }
        };
    }

    private OtlpTraceSpanEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OtlpTraceSpanEventMapper(
                new OtlpTraceEventMapper(new ObjectMapper(), 8192),
                TEST_REGISTRY,
                new OtlpMessagingTypeResolver(TEST_REGISTRY),
                new OtlpClientTypeResolver(TEST_REGISTRY),
                new OtlpEnvoyTypeResolver(TEST_REGISTRY),
                new OtlpExceptionInfoResolver(),
                new OtlpAttributeBoMapper(8192),
                8192);
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

    // =======================================================================
    // getClientSpanToEndPoint
    // =======================================================================

    @Test
    void getClientSpanToEndPoint_serverAddressWithPort() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, AttributeValue.of("db.example.com"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, AttributeValue.of(5432L)
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("db.example.com:5432");
    }

    @Test
    void getClientSpanToEndPoint_fallsBackToNetworkPeerAddress() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, AttributeValue.of("10.0.0.1"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, AttributeValue.of(5432L)
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("10.0.0.1:5432");
    }

    @Test
    void getClientSpanToEndPoint_serverAddressPreferredOverNetworkPeer() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, AttributeValue.of("db.example.com"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, AttributeValue.of(5432L),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, AttributeValue.of("10.0.0.1"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, AttributeValue.of(9999L)
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("db.example.com:5432");
    }

    @Test
    void getClientSpanToEndPoint_fallsBackToUpstreamAddress() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_UPSTREAM_ADDRESS, AttributeValue.of("proxy:9000")
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("proxy:9000");
    }

    @Test
    void getClientSpanToEndPoint_fallsBackToNetPeerName() {
        // Legacy semconv client (e.g. otel-js gRPC) emits only net.peer.name/net.peer.port —
        // previously resolved to null, dropping the ServerMap node.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_NAME, AttributeValue.of("cart"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_PORT, AttributeValue.of(7070L)
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("cart:7070");
    }

    @Test
    void getClientSpanToEndPoint_netPeerNameWithoutPort() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_NAME, AttributeValue.of("cart")
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("cart");
    }

    @Test
    void getClientSpanToEndPoint_serverAddressPreferredOverNetPeerName() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, AttributeValue.of("cart.svc"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, AttributeValue.of(7070L),
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_NAME, AttributeValue.of("cart"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_PORT, AttributeValue.of(9999L)
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("cart.svc:7070");
    }

    @Test
    void getClientSpanToEndPoint_fallsBackToHttpUrl() {
        // Legacy HTTP client emitting only http.url (seen from python-requests instrumentations).
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, AttributeValue.of("http://frontend-proxy:8080/api/cart")
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("frontend-proxy:8080");
    }

    @Test
    void getClientSpanToEndPoint_urlFullPreferredOverHttpUrl() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_URL_FULL, AttributeValue.of("https://api.example.com/users?id=1"),
                OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, AttributeValue.of("http://legacy.example.com/users")
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("api.example.com");
    }

    @Test
    void getClientSpanToEndPoint_urlPreferredOverSocketAddress() {
        // URL-derived logical host groups the ServerMap better than the socket IP.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_URL_FULL, AttributeValue.of("http://api.example.com:8080/v1"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, AttributeValue.of("10.0.0.1"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, AttributeValue.of(8080L)
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("api.example.com:8080");
    }

    @Test
    void getClientSpanToEndPoint_hostlessUrl_fallsThrough() {
        // A malformed URL with no extractable host must not shadow later fallbacks.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_URL_FULL, AttributeValue.of("https://"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, AttributeValue.of("10.0.0.1"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, AttributeValue.of(8080L)
        );
        assertThat(mapper.getClientSpanToEndPoint(attrs, new HashSet<>())).isEqualTo("10.0.0.1:8080");
    }

    // =======================================================================
    // getClientSpanToDestinationId
    // =======================================================================

    @Test
    void getClientSpanToDestinationId_prefersDbNamespace() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAMESPACE, AttributeValue.of("shop_db"),
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAME, AttributeValue.of("legacy_db"),
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("postgresql")
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("shop_db");
    }

    @Test
    void getClientSpanToDestinationId_fallsBackToDbName() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAME, AttributeValue.of("legacy_db"),
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("mysql")
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("legacy_db");
    }

    @Test
    void getClientSpanToDestinationId_namespacelessDb_usesDbSystemName() {
        // Redis-style DB with no namespace — group ServerMap node by system.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM_NAME, AttributeValue.of("redis"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, AttributeValue.of("cache-01.example.com"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, AttributeValue.of(6379L)
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("redis");
    }

    @Test
    void getClientSpanToDestinationId_namespacelessDb_fallsBackToDbSystem() {
        // 1.x semconv key still works as a fallback when 2.x key is absent.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("elasticsearch")
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("elasticsearch");
    }

    @Test
    void getClientSpanToDestinationId_redis_skipsDbNamespace() {
        // OTel Redis semconv uses db.namespace as the numeric DB index (0-15).
        // Treating that as destinationId would produce useless labels ("15") on the
        // ServerMap — instead, fall through to db.system so all Redis traffic groups
        // under a single "redis" node.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM_NAME, AttributeValue.of("redis"),
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAMESPACE, AttributeValue.of("15"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, AttributeValue.of("cache-01.example.com"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, AttributeValue.of(6379L)
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("redis");
    }

    @Test
    void getClientSpanToDestinationId_redis_skipsLegacyDbName() {
        // 1.x SDKs may emit db.name = "0" for the Redis DB index. Skip that too.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("redis"),
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAME, AttributeValue.of("0")
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("redis");
    }

    @Test
    void getClientSpanToDestinationId_redis_caseInsensitiveSystem() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM_NAME, AttributeValue.of("Redis"),
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_NAMESPACE, AttributeValue.of("0")
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("Redis");
    }

    @Test
    void getClientSpanToDestinationId_fallsBackToServerAddress_whenNoDbAttributes() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_ADDRESS, AttributeValue.of("svc.example.com"),
                OtlpTraceConstants.ATTRIBUTE_KEY_SERVER_PORT, AttributeValue.of(8080L)
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("svc.example.com:8080");
    }

    @Test
    void getClientSpanToDestinationId_fallsBackToNetworkPeerAddress() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_IP, AttributeValue.of("10.0.0.1"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NETWORK_PEER_PORT, AttributeValue.of(5432L)
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("10.0.0.1:5432");
    }

    @Test
    void getClientSpanToDestinationId_fallsBackToNetPeerName() {
        // Legacy semconv gRPC client — destinationId mirrors the endPoint fallback.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_NAME, AttributeValue.of("cart"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_PORT, AttributeValue.of(7070L)
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("cart:7070");
    }

    @Test
    void getClientSpanToDestinationId_fallsBackToUrl() {
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_HTTP_URL, AttributeValue.of("http://frontend-proxy:8080/api/cart")
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("frontend-proxy:8080");
    }

    @Test
    void getClientSpanToDestinationId_dbSystemPreferredOverNetPeerName() {
        // DB destination grouping (db.system) must not be shadowed by the new fallbacks.
        Map<String, AttributeValue> attrs = Map.of(
                OtlpTraceConstants.ATTRIBUTE_KEY_DB_SYSTEM, AttributeValue.of("redis"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_NAME, AttributeValue.of("cache-01"),
                OtlpTraceConstants.ATTRIBUTE_KEY_NET_PEER_PORT, AttributeValue.of(6379L)
        );
        assertThat(mapper.getClientSpanToDestinationId(attrs, new HashSet<>())).isEqualTo("redis");
    }

    // =======================================================================
    // map() — producer / internal endPoint & destinationId resolution
    // =======================================================================

    private static final byte[] TRACE_ID = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
    };
    private static final byte[] SPAN_ID = {1, 2, 3, 4, 5, 6, 7, 8};

    private static Span span(Span.SpanKind kind, KeyValue... attrs) {
        Span.Builder builder = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(kind.getNumber());
        for (KeyValue attr : attrs) {
            builder.addAttributes(attr);
        }
        return builder.build();
    }

    private SpanEventBo mapSingle(Span span) {
        return mapper.map(0L, span).get(0);
    }

    private static List<String> attributeKeys(SpanEventBo event) {
        return event.getAttributeBoList().stream()
                .map(AttributeBo::getKey)
                .toList();
    }

    private static Object findAnnotation(SpanEventBo event, int code) {
        return event.getAnnotationBoList().stream()
                .filter(a -> a.getKey() == code)
                .map(com.navercorp.pinpoint.common.server.bo.AnnotationBo::getValue)
                .findFirst()
                .orElse(null);
    }

    private static final int HTTP_STATUS_CODE =
            com.navercorp.pinpoint.common.trace.AnnotationKey.HTTP_STATUS_CODE.getCode();

    @Test
    void map_client_legacyGrpc_netPeerName_setsEndPointAndDestinationId_andFiltersRawKeys() {
        // End-to-end: a legacy-semconv gRPC client span (net.peer.name/net.peer.port only, as
        // emitted by otel-js) resolves endPoint/destinationId instead of dropping the node, and
        // the consumed keys are filtered from the raw attribute list (mirroring server.address).
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("rpc.system", strVal("grpc")),
                kv("rpc.service", strVal("oteldemo.CartService")),
                kv("net.peer.name", strVal("cart")),
                kv("net.peer.port", intVal(7070)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("cart:7070");
        assertThat(event.getDestinationId()).isEqualTo("cart:7070");
        assertThat(attributeKeys(event)).doesNotContain("net.peer.name", "net.peer.port");
    }

    @Test
    void map_client_httpUrlOnly_setsEndPoint_andKeepsRawUrl() {
        // Legacy HTTP client emitting only http.url — endPoint is derived from the URL host,
        // while the raw URL attribute is retained (path/query carry extra information).
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("http.url", strVal("http://frontend-proxy:8080/api/cart")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("frontend-proxy:8080");
        assertThat(event.getDestinationId()).isEqualTo("frontend-proxy:8080");
        assertThat(attributeKeys(event)).contains("http.url");
    }

    @Test
    void map_client_envoyEgress_consumedKeysFiltered() {
        // Envoy egress: upstream_cluster.name is promoted (destinationId + upstream.cluster
        // annotation) and upstream_address resolves the endPoint — both consumed → filtered.
        // response_flags is only the detection gate and stays raw.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("upstream_cluster.name", strVal("frontend")),
                kv("upstream_address", strVal("172.18.0.27:8080")),
                kv("response_flags", strVal("-")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getDestinationId()).isEqualTo("frontend");
        assertThat(event.getEndPoint()).isEqualTo("172.18.0.27:8080");
        assertThat(attributeKeys(event)).doesNotContain("upstream_cluster.name", "upstream_address");
        assertThat(attributeKeys(event)).contains("response_flags");
    }

    @Test
    void map_internal_netPeerName_notConsumed_keptRaw() {
        // INTERNAL spans resolve no endPoint/destinationId — net.peer.name is not consumed,
        // so it stays in the raw attribute list instead of being silently dropped.
        Span span = span(Span.SpanKind.SPAN_KIND_INTERNAL,
                kv("net.peer.name", strVal("cart")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isNull();
        assertThat(attributeKeys(event)).contains("net.peer.name");
    }

    @Test
    void map_client_httpStatusCode_promotedToAnnotation_andConsumedKeyFiltered() {
        // Mirrors native HTTP client plugins: the client SpanEvent promotes the status to the
        // HTTP_STATUS_CODE annotation and the consumed raw key is removed (no duplicate).
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("http.status_code", intVal(200)));

        SpanEventBo event = mapSingle(span);
        assertThat(findAnnotation(event, HTTP_STATUS_CODE)).isEqualTo(200);
        assertThat(attributeKeys(event)).doesNotContain("http.status_code");
    }

    @Test
    void map_client_bothStatusKeys_onlyConsumedKeyRemoved() {
        // http.response.status_code wins (precedence) and is promoted + removed; the non-consumed
        // legacy http.status_code is retained as a raw attribute.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("http.response.status_code", intVal(500)),
                kv("http.status_code", intVal(500)));

        SpanEventBo event = mapSingle(span);
        assertThat(findAnnotation(event, HTTP_STATUS_CODE)).isEqualTo(500);
        assertThat(attributeKeys(event)).doesNotContain("http.response.status_code");
        assertThat(attributeKeys(event)).contains("http.status_code");
    }

    @Test
    void map_client_grpc_noHttpStatusAnnotation() {
        // gRPC clients carry rpc.grpc.status_code, not an http.* status → no HTTP_STATUS_CODE
        // annotation is promoted.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("rpc.system", strVal("grpc")),
                kv("rpc.grpc.status_code", intVal(0)));

        SpanEventBo event = mapSingle(span);
        assertThat(findAnnotation(event, HTTP_STATUS_CODE)).isNull();
    }

    @Test
    void map_internal_httpStatusCode_promotedToAnnotation() {
        // A Next.js INTERNAL span (Node.runHandler) carries http.status_code; it is promoted to the
        // HTTP_STATUS_CODE annotation just like the client path (uniform: status present → surfaced).
        Span span = span(Span.SpanKind.SPAN_KIND_INTERNAL,
                kv("http.status_code", intVal(200)));

        SpanEventBo event = mapSingle(span);
        assertThat(findAnnotation(event, HTTP_STATUS_CODE)).isEqualTo(200);
        assertThat(attributeKeys(event)).doesNotContain("http.status_code");
    }

    @Test
    void map_client_httpMethod_promotedToAnnotation_andFiltered() {
        // HTTP client SpanEvent: the request method is promoted to the HTTP_METHOD annotation and
        // removed from the raw attributes.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("http.request.method", strVal("GET")));

        SpanEventBo event = mapSingle(span);
        assertThat(findAnnotation(event, AnnotationKey.HTTP_METHOD.getCode())).isEqualTo("GET");
        assertThat(attributeKeys(event)).doesNotContain("http.request.method");
    }

    @Test
    void map_producer_kafka_endPointFromBroker_destinationIdFromTopic() {
        Span span = span(Span.SpanKind.SPAN_KIND_PRODUCER,
                kv("messaging.system", strVal("kafka")),
                kv("messaging.destination.name", strVal("orders")),
                kv("server.address", strVal("broker1.example.com")),
                kv("server.port", intVal(9092)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("broker1.example.com:9092");
        assertThat(event.getDestinationId()).isEqualTo("orders");
    }

    @Test
    void map_producer_endPoint_fallsBackToNetworkPeerAddress() {
        Span span = span(Span.SpanKind.SPAN_KIND_PRODUCER,
                kv("messaging.system", strVal("kafka")),
                kv("messaging.destination.name", strVal("orders")),
                kv("network.peer.address", strVal("10.0.0.1")),
                kv("network.peer.port", intVal(9092)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("10.0.0.1:9092");
        assertThat(event.getDestinationId()).isEqualTo("orders");
    }

    @Test
    void map_producer_endPoint_fallsBackToClientId_whenBrokerAbsent() {
        Span span = span(Span.SpanKind.SPAN_KIND_PRODUCER,
                kv("messaging.system", strVal("kafka")),
                kv("messaging.destination.name", strVal("orders")),
                kv("messaging.client_id", strVal("producer-1")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("producer-1");
        assertThat(event.getDestinationId()).isEqualTo("orders");
    }

    @Test
    void map_producer_destinationId_fallsBackToEndPoint_whenDestinationNameAbsent() {
        Span span = span(Span.SpanKind.SPAN_KIND_PRODUCER,
                kv("messaging.system", strVal("kafka")),
                kv("server.address", strVal("broker1.example.com")),
                kv("server.port", intVal(9092)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("broker1.example.com:9092");
        assertThat(event.getDestinationId()).isEqualTo("broker1.example.com:9092");
    }

    @Test
    void map_producer_rabbitmq_destinationIdIsExchange() {
        Span span = span(Span.SpanKind.SPAN_KIND_PRODUCER,
                kv("messaging.system", strVal("rabbitmq")),
                kv("messaging.destination.name", strVal("orders.exchange")),
                kv("server.address", strVal("rabbit1.example.com")),
                kv("server.port", intVal(5672)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("rabbit1.example.com:5672");
        assertThat(event.getDestinationId()).isEqualTo("orders.exchange");
    }

    @Test
    void map_producer_activemq_destinationIdIsQueueName() {
        Span span = span(Span.SpanKind.SPAN_KIND_PRODUCER,
                kv("messaging.system", strVal("activemq")),
                kv("messaging.destination.name", strVal("orders.queue")),
                kv("server.address", strVal("amq1.example.com")),
                kv("server.port", intVal(61616)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isEqualTo("amq1.example.com:61616");
        assertThat(event.getDestinationId()).isEqualTo("orders.queue");
    }

    @Test
    void map_client_grpc_setsGrpcServiceType() {
        // OTel grpc-1.6 agent emits rpc.system="grpc" on the client-side instrumenter.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("rpc.system", strVal("grpc")),
                kv("rpc.service", strVal("orders.OrderService")),
                kv("rpc.method", strVal("PlaceOrder")),
                kv("server.address", strVal("orders.example.com")),
                kv("server.port", intVal(8080)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo((short) 9160); // GRPC
    }

    @Test
    void map_client_dubbo_setsDubboConsumerServiceType() {
        // OTel apache-dubbo-2.7 agent emits rpc.system="apache_dubbo" on the client side.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("rpc.system", strVal("apache_dubbo")),
                kv("rpc.service", strVal("com.example.UserService")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo((short) 9997); // APACHE_DUBBO_CONSUMER
    }

    @Test
    void map_client_http_keepsOpenTelemetryClient() {
        // Generic HTTP client (Apache HttpClient / OkHttp / java-http-client / async-http-client)
        // emits no rpc.system and no framework identifier — stays on OPENTELEMETRY_CLIENT.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("http.request.method", strVal("GET")),
                kv("url.full", strVal("https://api.example.com/users/123")),
                kv("network.protocol.name", strVal("http")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void map_client_unsupportedRpcSystem_keepsOpenTelemetryClient() {
        // OTel rpc.system values without a Pinpoint client counterpart.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("rpc.system", strVal("java_rmi")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    @Test
    void map_client_grpc_caseInsensitive() {
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("rpc.system", strVal("GRPC")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo((short) 9160);
    }

    @Test
    void map_client_envoy_setsEnvoyEgressAndUpstreamClusterAnnotation() {
        // Envoy egress leg over OTLP: upstream_cluster tags trigger the Envoy branch, which
        // overrides the generic OPENTELEMETRY_CLIENT with ENVOY_EGRESS and records the
        // upstream.cluster / envoy.operation annotations.
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("response_flags", strVal("-")),
                kv("http.status_code", strVal("200")),
                kv("upstream_cluster", strVal("frontend")),
                kv("upstream_cluster.name", strVal("frontend")),
                kv("upstream_address", strVal("172.18.0.27:8080")));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo((short) 9302); // ENVOY_EGRESS
        assertThat(event.getEndPoint()).isEqualTo("172.18.0.27:8080");
        assertThat(event.getDestinationId()).isEqualTo("frontend");
        assertThat(annotationValue(event.getAnnotationBoList(), OtlpTraceConstants.ANNOTATION_KEY_UPSTREAM_CLUSTER))
                .isEqualTo("frontend");
        assertThat(annotationValue(event.getAnnotationBoList(), OtlpTraceConstants.ANNOTATION_KEY_ENVOY_OPERATION))
                .isEqualTo("Egress");
    }

    @Test
    void map_client_envoy_absentEnvoyType_fallsBackToOpenTelemetryClient() {
        // A collector without the envoy-type-provider still ingests the span; it just keeps the
        // generic OPENTELEMETRY_CLIENT type (resolver fallback), never failing.
        OtlpTraceSpanEventMapper noEnvoy = new OtlpTraceSpanEventMapper(
                new OtlpTraceEventMapper(new ObjectMapper(), 8192),
                TEST_REGISTRY,
                new OtlpMessagingTypeResolver(TEST_REGISTRY),
                new OtlpClientTypeResolver(TEST_REGISTRY),
                new OtlpEnvoyTypeResolver(buildRegistryWithout("ENVOY_EGRESS")),
                new OtlpExceptionInfoResolver(),
                new OtlpAttributeBoMapper(8192),
                8192);
        Span span = span(Span.SpanKind.SPAN_KIND_CLIENT,
                kv("upstream_cluster.name", strVal("frontend")));

        SpanEventBo event = noEnvoy.map(0L, span).get(0);
        assertThat(event.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_CLIENT.getCode());
    }

    private static Object annotationValue(List<com.navercorp.pinpoint.common.server.bo.AnnotationBo> annotations, int key) {
        return annotations.stream()
                .filter(a -> a.getKey() == key)
                .map(com.navercorp.pinpoint.common.server.bo.AnnotationBo::getValue)
                .findFirst()
                .orElse(null);
    }

    private static ServiceTypeRegistryService buildRegistryWithout(String excludedName) {
        Map<String, ServiceType> byName = new HashMap<>();
        byName.put("GRPC", ServiceTypeFactory.of(9160, "GRPC", "GRPC"));
        byName.remove(excludedName);
        return new ServiceTypeRegistryService() {
            @Override
            public ServiceType findServiceType(int serviceType) {
                return byName.values().stream()
                        .filter(t -> t.getCode() == serviceType)
                        .findFirst()
                        .orElse(ServiceType.UNDEFINED);
            }

            @Override
            public ServiceType findServiceTypeByName(String typeName) {
                return byName.getOrDefault(typeName, ServiceType.UNDEFINED);
            }

            @Override
            public List<ServiceType> findDesc(String desc) {
                return List.of();
            }
        };
    }

    @Test
    void map_internal_endPointAndDestinationIdAreNull() {
        // Internal span has no outgoing endpoint/destination, even when server.address is present
        // (the generic HTTP/DB fallback no longer leaks into internal-kind spans).
        Span span = span(Span.SpanKind.SPAN_KIND_INTERNAL,
                kv("server.address", strVal("example.com")),
                kv("server.port", intVal(8080)));

        SpanEventBo event = mapSingle(span);
        assertThat(event.getEndPoint()).isNull();
        assertThat(event.getDestinationId()).isNull();
        assertThat(event.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_INTERNAL.getCode());
    }

    @Test
    void map_unspecified_kind_usesInternalMethodFallback() {
        // OTel SDK that doesn't set span.kind (or sets UNSPECIFIED) lands in the else branch
        // but is NOT a true OTel INTERNAL — fall back to the generic INTERNAL_METHOD type to
        // signal the missing classification.
        Span span = span(Span.SpanKind.SPAN_KIND_UNSPECIFIED);

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo(ServiceType.INTERNAL_METHOD.getCode());
    }

    @Test
    void map_nestedServerKind_usesInternalMethodFallback() {
        // SERVER kind nested under another span reaches the SpanEvent mapper. It's neither
        // database, client, producer, nor explicit OTel INTERNAL → INTERNAL_METHOD fallback.
        Span span = span(Span.SpanKind.SPAN_KIND_SERVER);

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo(ServiceType.INTERNAL_METHOD.getCode());
    }

    @Test
    void map_nestedConsumerKind_usesInternalMethodFallback() {
        // CONSUMER kind nested under another span — same fallthrough as nested SERVER.
        Span span = span(Span.SpanKind.SPAN_KIND_CONSUMER);

        SpanEventBo event = mapSingle(span);
        assertThat(event.getServiceType()).isEqualTo(ServiceType.INTERNAL_METHOD.getCode());
    }

    // =======================================================================
    // map() — SDK-side dropped count composite annotation on SpanEvent
    // =======================================================================

    @Test
    void map_event_droppedAnnotation_omitsZeroComponents() {
        Span span = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CLIENT_VALUE)
                .setDroppedAttributesCount(4)
                .setDroppedLinksCount(1)
                .build();

        SpanEventBo event = mapSingle(span);
        Object dropped = event.getAnnotationBoList().stream()
                .filter(a -> a.getKey() == com.navercorp.pinpoint.common.trace.AnnotationKey.OPENTELEMETRY_DROPPED.getCode())
                .map(com.navercorp.pinpoint.common.server.bo.AnnotationBo::getValue).findFirst().orElse(null);
        assertThat(dropped).isEqualTo("attributes=4 links=1");
    }

    @Test
    void map_event_droppedAnnotation_suppressedWhenAllZero() {
        Span span = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CLIENT_VALUE)
                .build();

        SpanEventBo event = mapSingle(span);
        assertThat(event.getAnnotationBoList())
                .extracting(com.navercorp.pinpoint.common.server.bo.AnnotationBo::getKey)
                .doesNotContain(com.navercorp.pinpoint.common.trace.AnnotationKey.OPENTELEMETRY_DROPPED.getCode());
    }

    // =======================================================================
    // map() — error status → SpanEvent exceptionInfo (className:message encoding)
    // =======================================================================

    private static Span.Event exceptionEvent(KeyValue... attrs) {
        Span.Event.Builder event = Span.Event.newBuilder().setName("exception");
        for (KeyValue attr : attrs) {
            event.addAttributes(attr);
        }
        return event.build();
    }

    private static Span errorClientSpan(Status.StatusCode code, String statusMessage, KeyValue[] attrs, Span.Event... events) {
        Span.Builder builder = Span.newBuilder()
                .setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(SPAN_ID))
                .setKindValue(Span.SpanKind.SPAN_KIND_CLIENT_VALUE)
                .setStatus(Status.newBuilder().setCode(code).setMessage(statusMessage == null ? "" : statusMessage));
        for (KeyValue attr : attrs) {
            builder.addAttributes(attr);
        }
        for (Span.Event event : events) {
            builder.addEvents(event);
        }
        return builder.build();
    }

    private static long countEventAnnotations(SpanEventBo event) {
        return event.getAnnotationBoList().stream()
                .filter(a -> a.getKey() == com.navercorp.pinpoint.common.trace.AnnotationKey.OPENTELEMETRY_EVENT.getCode())
                .count();
    }

    @Test
    void map_error_exceptionEvent_setsExceptionInfo_andSkipsEventAnnotation() {
        Span span = errorClientSpan(Status.StatusCode.STATUS_CODE_ERROR, "ignored", new KeyValue[]{},
                exceptionEvent(kv("exception.type", strVal("java.io.IOException")),
                        kv("exception.message", strVal("disk full"))));

        SpanEventBo event = mapSingle(span);

        assertThat(event.hasException()).isTrue();
        assertThat(event.getExceptionInfo().id()).isEqualTo(0);
        assertThat(event.getExceptionInfo().message()).isEqualTo("java.io.IOException:disk full");
        assertThat(countEventAnnotations(event)).isZero();
    }

    @Test
    void map_error_noEvent_statusMessageOnly_emptyClassPrefix() {
        Span span = errorClientSpan(Status.StatusCode.STATUS_CODE_ERROR, "Connection refused: host", new KeyValue[]{});

        SpanEventBo event = mapSingle(span);

        assertThat(event.getExceptionInfo().message()).isEqualTo(":Connection refused: host");
    }

    @Test
    void map_error_noSignal_noExceptionInfo() {
        Span span = errorClientSpan(Status.StatusCode.STATUS_CODE_ERROR, "", new KeyValue[]{});

        SpanEventBo event = mapSingle(span);

        assertThat(event.hasException()).isFalse();
    }

    @Test
    void map_okStatus_noExceptionInfo_keepsEventAnnotation() {
        Span span = errorClientSpan(Status.StatusCode.STATUS_CODE_UNSET, "", new KeyValue[]{},
                exceptionEvent(kv("exception.type", strVal("java.lang.RuntimeException"))));

        SpanEventBo event = mapSingle(span);

        assertThat(event.hasException()).isFalse();
        assertThat(countEventAnnotations(event)).isEqualTo(1);
    }
}
