/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common;

import static com.navercorp.pinpoint.common.HistogramSchema.*;
import static com.navercorp.pinpoint.common.ServiceTypeConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.common.util.RpcCodeRange;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;

/**
 * @author emeroad
 * @author netspider
 */
public enum ServiceType {


    // Undefined Service Code
	UNDEFINED((short) -1, "UNDEFINED", TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
	
    // Callee node that agent hasn't been installed
    UNKNOWN((short) 1, "UNKNOWN", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    // User
    USER((short) 2, "USER", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    // Group of UNKNOWN,  used only for UI
    UNKNOWN_GROUP((short) 3, "UNKNOWN_GROUP", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    
    // Group of TEST, used for running tests
    TEST((short) 5, "TEST", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    // Java applications, WAS
    STAND_ALONE((short) 1000, "STAND_ALONE", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    TEST_STAND_ALONE((short) 1005, "TEST_STAND_ALONE", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    TOMCAT((short) 1010, "TOMCAT", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    BLOC((short) 1020, "BLOC", !TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    BLOC_INTERNAL_METHOD((short) 1021, "INTERNAL_METHOD", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    /**
     * Database
     * shown only as xxx_EXECUTE_QUERY at the statistics info section in the server map 
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

    // Internal method
    // FIXME it's not clear to put internal method here. but do that for now.
    INTERNAL_METHOD((short) 5000, "INTERNAL_METHOD", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    // Spring framework
    SPRING((short) 5050, "SPRING", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    SPRING_MVC((short) 5051, "SPRING", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    // FIXME need to define how to handle spring related codes
    SPRING_ORM_IBATIS((short) 5061, "SPRING", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    SPRING_BEAN((short) 5071, "SPRING_BEAN", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    // xBatis
    IBATIS((short) 5500, "IBATIS", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    MYBATIS((short) 5510, "MYBATIS", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),
    
    // DBCP
    DBCP((short) 6050, "DBCP", !TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, NORMAL_SCHEMA),

    // Memory cache
    MEMCACHED((short) 8050, "MEMCACHED", TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, FAST_SCHEMA),
    MEMCACHED_FUTURE_GET((short) 8051, "MEMCACHED", TERMINAL, !RECORD_STATISTICS, !INCLUDE_DESTINATION, FAST_SCHEMA),
    ARCUS((short) 8100, "ARCUS", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),
    ARCUS_FUTURE_GET((short) 8101, "ARCUS", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),
    ARCUS_EHCACHE_FUTURE_GET((short) 8102, "ARCUS-EHCACHE", TERMINAL, !RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),

    // Redis & nBase-ARC
    REDIS((short) 8200, "REDIS", TERMINAL, RECORD_STATISTICS, !INCLUDE_DESTINATION, FAST_SCHEMA),
    NBASE_ARC((short) 8250, "NBASE_ARC", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION, FAST_SCHEMA),
    
    
    // Connector, Client
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
    
    // FIXME record statistics of only rpc call currently. so is it all right to chane into isRecordRpc()
    private final boolean recordStatistics;
    
    // whether or not print out api including destinationId
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

    // FIXME it may be not good to find serviceType by using this api
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

    // FIXME record statistics of only rpc call currently. so is it all right to chane into isRecordRpc()
    public boolean isRecordStatistics() {
        return recordStatistics;
    }

	public boolean isUnknown() {
		return this == ServiceType.UNKNOWN; // || this == ServiceType.UNKNOWN_CLOUD;
	}
    

    // return true when the service type is USER or can not be identified
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

        // Don't modify
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
