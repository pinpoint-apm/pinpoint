package com.nhn.pinpoint.common;

import static com.nhn.pinpoint.common.HistogramSchema.FAST_SCHEMA;
import static com.nhn.pinpoint.common.HistogramSchema.NORMAL_SCHEMA;
import static com.nhn.pinpoint.common.ServiceTypeConstants.INCLUDE_DESTINATION;
import static com.nhn.pinpoint.common.ServiceTypeConstants.RECORD_STATISTICS;
import static com.nhn.pinpoint.common.ServiceTypeConstants.TERMINAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.common.util.RpcCodeRange;
import com.nhn.pinpoint.common.util.apache.IntHashMap;

/**
 * @author emeroad
 * @author netspider
 */
public enum ServiceType {

	/**
	 * 정의되지 않은 서비스 코드,
	 */
	UNDEFINED((short) -1, "UNDEFINED", TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
	
	/**
	 * agent가 설치되지 않은 피호출자.
	 */
    UNKNOWN((short) 1, "UNKNOWN", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    
    /**
     * 사용자
     */
    USER((short) 2, "USER", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    
    /**
     * UNKNOWN의 그룹, UI에서만 사용함.
     */
    UNKNOWN_GROUP((short) 3, "UNKNOWN_GROUP", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    
    /**
     * TEST의 그룹 - 테스트 실행 시 사용
     */
    TEST((short) 5, "TEST", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    /**
     * Java applications, WAS
     */
    STAND_ALONE((short) 1000, "STAND_ALONE", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    TEST_STAND_ALONE((short) 1005, "TEST_STAND_ALONE", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    TOMCAT((short) 1010, "TOMCAT", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    BLOC((short) 1020, "BLOC", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    BLOC_INTERNAL_METHOD((short) 1021, "INTERNAL_METHOD", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    /**
     * Database
     * xxx_EXECUTE_QUERY만 server map통계정보에 집계된다.
     */
    // DB 2000
    UNKNOWN_DB((short) 2050, "UNKNOWN_DB", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),
    UNKNOWN_DB_EXECUTE_QUERY((short) 2051, "UNKNOWN_DB", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),

    MYSQL((short) 2100, "MYSQL", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),
    MYSQL_EXECUTE_QUERY((short) 2101, "MYSQL", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),

    MSSQL((short) 2200, "MSSQLSERVER", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),
    MSSQL_EXECUTE_QUERY((short) 2201, "MSSQLSERVER", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),

    ORACLE((short) 2300, "ORACLE", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),
    ORACLE_EXECUTE_QUERY((short) 2301, "ORACLE", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),

    CUBRID((short) 2400, "CUBRID", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, NORMAL_SCHEMA),
    CUBRID_EXECUTE_QUERY((short) 2401, "CUBRID", TERMINAL, RECORD_STATISTICS, true, NORMAL_SCHEMA),

    /**
     * Internal method
     */
    // FIXME internal method를 여기에 넣기 애매하긴 하나.. 일단 그대로 둠.
    INTERNAL_METHOD((short) 5000, "INTERNAL_METHOD", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    /**
     * Spring framework
     */
    SPRING((short) 5050, "SPRING", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    SPRING_MVC((short) 5051, "SPRING", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    // FIXME 스프링 관련 코드들 어떻게 가져갈지 정리 필요
    SPRING_ORM_IBATIS((short) 5061, "SPRING", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    SPRING_BEAN((short) 5071, "SPRING_BEAN", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    
    /**
     * xBatis
     */
    IBATIS((short) 5500, "IBATIS", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    MYBATIS((short) 5510, "MYBATIS", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    
    /**
     * DBCP
     */
    DBCP((short) 6050, "DBCP", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    /**
     * Memory cache
     */
    MEMCACHED((short) 8050, "MEMCACHED", TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, FAST_SCHEMA),
    MEMCACHED_FUTURE_GET((short) 8051, "MEMCACHED", TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, FAST_SCHEMA),
    ARCUS((short) 8100, "ARCUS", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),
    ARCUS_FUTURE_GET((short) 8101, "ARCUS", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),
    ARCUS_EHCACHE_FUTURE_GET((short) 8102, "ARCUS-EHCACHE", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),

    /**
     * Redis & nBase-ARC
     */
    REDIS((short) 8200, "REDIS", TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, FAST_SCHEMA),
    NBASE_ARC((short) 8250, "NBASE_ARC", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),
    
    
    /**
     * Connector, Client
     */
    HTTP_CLIENT((short) 9050, "HTTP_CLIENT", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    HTTP_CLIENT_INTERNAL((short) 9051, "HTTP_CLIENT", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    JDK_HTTPURLCONNECTOR((short) 9055, "JDK_HTTPCONNECTOR", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
	NPC_CLIENT((short) 9060, "NPC_CLIENT", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
	NIMM_CLIENT((short) 9070, "NIMM_CLIENT", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA);

    public static final short WAS_START_INDEX = 1000;
    public static final short WAS_END_INDEX = 2000;

    private final short code;
    private final String desc;
    private final boolean terminal;
    
    // FIXME rpc 호출에 대해서만 통계정보를 남길 것이니, isRecordRpc()로 바꾸는건 어떨지???
    private final boolean recordStatistics;
    
    // DetinationId를 포함시켜 api를 출력하지 여부
    private final boolean includeDestinationId;
    private final HistogramSchema histogramSchema;

    ServiceType(short code, String desc, boolean terminal, boolean recordStatistics, boolean includeDestinationId, HistogramSchema histogramSchema) {
        this.code = code;
        this.desc = desc;
        this.terminal = terminal;
        this.recordStatistics = recordStatistics;
        this.includeDestinationId = includeDestinationId;
        this.histogramSchema = histogramSchema;
    }

    // FIXME 이 메소드를 사용해서 ServiceType을 찾는건 좋지 않을 듯.
    public static List<ServiceType> findDesc(String desc) {
        if (desc == null) {
            throw new NullPointerException("desc must not be null");
        }
        return STATISTICS_LOOKUP_TABLE.get(desc);
    }

    public boolean isInternalMethod() {
    	return this == INTERNAL_METHOD;
    }

    public boolean isRpcClient() {
        return RpcCodeRange.isRpcRange(code);
    }

    public boolean isIndexable() {
        return !terminal && !isRpcClient() && code > 1000;
    }

    // FIXME rpc 호출에 대해서만 통계정보를 남길 것이니, isRecordRpc()로 바꾸는건 어떨지???
    public boolean isRecordStatistics() {
        return recordStatistics;
    }

	/**
	 * agent가 설치되어있지 않은 피호출자인가?
	 * 
	 * @return
	 */
	public boolean isUnknown() {
		return this == ServiceType.UNKNOWN; // || this == ServiceType.UNKNOWN_CLOUD;
	}
    
    /**
     * 사용자 또는 알 수 없는 호출자인가?
     * 
     * @return
     */
    public boolean isUser() {
    	return this == ServiceType.USER;
    }

    public short getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public boolean isIncludeDestinationId() {
        return includeDestinationId;
    }

    public HistogramSchema getHistogramSchema() {
        return histogramSchema;
    }

	public boolean isWas() {
		return isWas(this.code);
	}

    public static boolean isWas(final short code) {
        return code >= WAS_START_INDEX && code < WAS_END_INDEX;
    }

    @Override
    public String toString() {
        return desc;
    }

    public static ServiceType findServiceType(short code) {
        ServiceType serviceType = CODE_LOOKUP_TABLE.get(code);
        if (serviceType == null) {
            return UNDEFINED;
        	//return UNKNOWN;
        }
        return serviceType;
    }

    private static final IntHashMap<ServiceType> CODE_LOOKUP_TABLE = new IntHashMap<ServiceType>(256);
    private static final Map<String, List<ServiceType>> STATISTICS_LOOKUP_TABLE = new HashMap<String, List<ServiceType>>(64);

    static {
        initializeLookupTable();
        initializeStatisticsLookupTable();
    }

    private static void initializeStatisticsLookupTable() {
        ServiceType[] values = ServiceType.values();
        final Map<String, List<ServiceType>> temp = new HashMap<String, List<ServiceType>>();
        for (ServiceType serviceType : values) {
            if(serviceType.isRecordStatistics()) {
                List<ServiceType> serviceTypeList = STATISTICS_LOOKUP_TABLE.get(serviceType.getDesc());
                if (serviceTypeList == null) {
                    serviceTypeList = new ArrayList<ServiceType>();
                    temp.put(serviceType.getDesc(), serviceTypeList);
                }
                serviceTypeList.add(serviceType);
            }
        }
        // 수정하지 못하도록 한다.
        for (Map.Entry<String, List<ServiceType>> entry : temp.entrySet()) {
            List<ServiceType> serviceTypes = Collections.unmodifiableList(entry.getValue());
            STATISTICS_LOOKUP_TABLE.put(entry.getKey(), serviceTypes);
        }

    }

    private static void initializeLookupTable() {
        ServiceType[] values = ServiceType.values();
        for (ServiceType serviceType : values) {
            ServiceType check = CODE_LOOKUP_TABLE.put(serviceType.code, serviceType);
            if (check != null) {
                throw new IllegalStateException("duplicated code found. code:" + serviceType.code);
            }
        }
    }
}
