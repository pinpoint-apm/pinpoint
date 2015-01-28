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

import static com.navercorp.pinpoint.common.AnnotationKeyMatcher.*;
import static com.navercorp.pinpoint.common.HistogramSchema.*;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;


/**
 * @author emeroad
 * @author netspider
 * @author Jongho Moon
 * 
 */
public class ServiceType {
    private final short code;
    private final String name;
    private final String desc;
    private final AnnotationKeyMatcher displayArgumentMatcher;
    private final boolean terminal;

    // FIXME record statistics of only rpc call currently. so is it all right to chane into isRecordRpc()
    private final boolean recordStatistics;

    // whether or not print out api including destinationId
    private final boolean includeDestinationId;
    private final HistogramSchema histogramSchema;
    
    public static ServiceType of(int code, String name, HistogramSchema histogramSchema, ServiceTypeProperty... properties) {
        return of(code, name, name, histogramSchema, properties);
    }
    
    public static ServiceType of(int code, String name, HistogramSchema histogramSchema, AnnotationKey displayArgument, ServiceTypeProperty... properties) {
        return of(code, name, name, histogramSchema, displayArgument, properties);
    }
    
    public static ServiceType of(int code, String name, HistogramSchema histogramSchema, AnnotationKeyMatcher displayArgumentMatcher, ServiceTypeProperty... properties) {
        return new ServiceType(code, name, name, histogramSchema, displayArgumentMatcher, properties);
    }

    public static ServiceType of(int code, String name, String desc, HistogramSchema histogramSchema, ServiceTypeProperty... properties) {
        return new ServiceType(code, name, desc, histogramSchema, NOTHING_MATCHER, properties);
    }
    
    public static ServiceType of(int code, String name, String desc, HistogramSchema histogramSchema, AnnotationKey displayArgument, ServiceTypeProperty... properties) {
        return new ServiceType(code, name, desc, histogramSchema, new AnnotationKeyMatcher.ExactMatcher(displayArgument), properties);
    }
    
    public ServiceType(int code, String name, String desc, HistogramSchema histogramSchema, AnnotationKeyMatcher displayArgumentMatcher, ServiceTypeProperty... properties) {
        // code must be a short value but constructors accept int to make declaring ServiceType values more cleaner by removing casting to short.
        if (code > Short.MAX_VALUE || code < Short.MIN_VALUE) {
            throw new IllegalArgumentException("code must be a short value");
        }
        
        this.code = (short)code;
        this.name = name;
        this.desc = desc;
        this.histogramSchema = histogramSchema;
        this.displayArgumentMatcher = displayArgumentMatcher;
        
        boolean terminal = false;
        boolean recordStatistics = false;
        boolean includeDestinationId = false;
        
        for (ServiceTypeProperty property : properties) {
            switch (property) {
            case TERMINAL:
                terminal = true;
                break;
                
            case RECORD_STATISTICS:
                recordStatistics = true;
                break;
                
            case INCLUDE_DESTINATION_ID:
                includeDestinationId = true;
                break;
            }
        }
        
        this.terminal = terminal;
        this.recordStatistics = recordStatistics;
        this.includeDestinationId = includeDestinationId;
    }

    public boolean isInternalMethod() {
        return this == INTERNAL_METHOD;
    }

    public boolean isRpcClient() {
        return ServiceTypeCategory.RPC.contains(code);
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

    public String getName() {
        return name;
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
    
    public AnnotationKeyMatcher getDisplayArgumentMatcher() {
        return displayArgumentMatcher;
    }

    @Override
    public String toString() {
        return desc;
    }

    // Undefined Service Code
    public static final ServiceType UNDEFINED = of(-1, "UNDEFINED", NORMAL_SCHEMA, TERMINAL);

    // Callee node that agent hasn't been installed
    public static final ServiceType UNKNOWN = of(1, "UNKNOWN", NORMAL_SCHEMA, RECORD_STATISTICS);

    // UserUNDEFINED
    public static final ServiceType USER = of(2, "USER", NORMAL_SCHEMA, RECORD_STATISTICS);

    // Group of UNKNOWN, used only for UI
    public static final ServiceType UNKNOWN_GROUP = of(3, "UNKNOWN_GROUP", NORMAL_SCHEMA, RECORD_STATISTICS);

    // Group of TEST, used for running tests
    public static final ServiceType TEST = of(5, "TEST", NORMAL_SCHEMA);

    // Java applications, WAS
    public static final ServiceType STAND_ALONE = of(1000, "STAND_ALONE", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType TEST_STAND_ALONE = of(1005, "TEST_STAND_ALONE", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType TOMCAT = of(1010, "TOMCAT", NORMAL_SCHEMA, RECORD_STATISTICS);

    /**
     * Database shown only as xxx_EXECUTE_QUERY at the statistics info section in the server map
     */
    // DB 2000
    public static final ServiceType UNKNOWN_DB = of(2050, "UNKNOWN_DB", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType UNKNOWN_DB_EXECUTE_QUERY = of(2051, "UNKNOWN_DB_EXECUTE_QUERY", "UNKNOWN_DB", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final ServiceType MYSQL = of(2100, "MYSQL", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType MYSQL_EXECUTE_QUERY = of(2101, "MYSQL_EXECUTE_QUERY", "MYSQL", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final ServiceType MSSQL = of(2200, "MSSQL", "MSSQLSERVER", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType MSSQL_EXECUTE_QUERY = of(2201, "MSSQL_EXECUTE_QUERY", "MSSQLSERVER", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final ServiceType ORACLE = of(2300, "ORACLE", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ORACLE_EXECUTE_QUERY = of(2301, "ORACLE_EXECUTE_QUERY", "ORACLE", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final ServiceType CUBRID = of(2400, "CUBRID", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType CUBRID_EXECUTE_QUERY = of(2401, "CUBRID_EXECUTE_QUERY", "CUBRID", NORMAL_SCHEMA, AnnotationKey.ARGS0, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    // Internal method
    // FIXME it's not clear to put internal method here. but do that for now.
    public static final ServiceType INTERNAL_METHOD = of(5000, "INTERNAL_METHOD", NORMAL_SCHEMA);

    // Spring framework
    public static final ServiceType SPRING = of(5050, "SPRING", NORMAL_SCHEMA);
    public static final ServiceType SPRING_MVC = of(5051, "SPRING_MVC", "SPRING", NORMAL_SCHEMA);
    // FIXME need to define how to handle spring related codes
    public static final ServiceType SPRING_ORM_IBATIS = of(5061, "SPRING_ORM_IBATIS", "SPRING", NORMAL_SCHEMA, AnnotationKey.ARGS0);
    public static final ServiceType SPRING_BEAN = of(5071, "SPRING_BEAN", "SPRING_BEAN", NORMAL_SCHEMA);

    // xBatis
    public static final ServiceType IBATIS = of(5500, "IBATIS", NORMAL_SCHEMA, AnnotationKey.ARGS0);
    public static final ServiceType MYBATIS = of(5510, "MYBATIS", NORMAL_SCHEMA, AnnotationKey.ARGS0);

    // DBCP
    public static final ServiceType DBCP = of(6050, "DBCP", NORMAL_SCHEMA);

    // Memory cache
    public static final ServiceType MEMCACHED = of(8050, "MEMCACHED", FAST_SCHEMA, ARGS_MATCHER, TERMINAL, RECORD_STATISTICS);
    public static final ServiceType MEMCACHED_FUTURE_GET = of(8051, "MEMCACHED_FUTURE_GET", "MEMCACHED", FAST_SCHEMA, TERMINAL);
    public static final ServiceType ARCUS = of(8100, "ARCUS", FAST_SCHEMA, ARGS_MATCHER, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_FUTURE_GET = of(8101, "ARCUS_FUTURE_GET", "ARCUS", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_EHCACHE_FUTURE_GET = of(8102, "ARCUS_EHCACHE_FUTURE_GET", "ARCUS-EHCACHE", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);

    // Redis & nBase-ARC
    public static final ServiceType REDIS = of(8200, "REDIS", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS);
    public static final ServiceType NBASE_ARC = of(8250, "NBASE_ARC", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    // Connector, Client
    public static final ServiceType HTTP_CLIENT = of(9050, "HTTP_CLIENT", NORMAL_SCHEMA, AnnotationKey.HTTP_URL, RECORD_STATISTICS);
    public static final ServiceType HTTP_CLIENT_INTERNAL = of(9051, "HTTP_CLIENT_INTERNAL", "HTTP_CLIENT", NORMAL_SCHEMA, AnnotationKey.HTTP_CALL_RETRY_COUNT);
    public static final ServiceType JDK_HTTPURLCONNECTOR = of(9055, "JDK_HTTPURLCONNECTOR", "JDK_HTTPCONNECTOR", NORMAL_SCHEMA, AnnotationKey.HTTP_URL, RECORD_STATISTICS);
    public static final ServiceType NPC_CLIENT = of(9060, "NPC_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType NIMM_CLIENT = of(9070, "NIMM_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);

    static final List<ServiceType> DEFAULT_VALUES = Collections.unmodifiableList(Arrays.asList(
        UNDEFINED,
        UNKNOWN,
        USER,
        UNKNOWN_GROUP,
        TEST,
        STAND_ALONE,
        TEST_STAND_ALONE,
        TOMCAT,
        UNKNOWN_DB,
        UNKNOWN_DB_EXECUTE_QUERY,
        MYSQL,
        MYSQL_EXECUTE_QUERY,
        MSSQL,
        MSSQL_EXECUTE_QUERY,
        ORACLE,
        ORACLE_EXECUTE_QUERY,
        CUBRID,
        CUBRID_EXECUTE_QUERY,
        INTERNAL_METHOD,
        SPRING,
        SPRING_MVC,
        SPRING_ORM_IBATIS,
        SPRING_BEAN,
        IBATIS,
        MYBATIS,
        DBCP,
        MEMCACHED,
        MEMCACHED_FUTURE_GET,
        ARCUS,
        ARCUS_FUTURE_GET,
        ARCUS_EHCACHE_FUTURE_GET,
        REDIS,
        NBASE_ARC,
        HTTP_CLIENT,
        HTTP_CLIENT_INTERNAL,
        JDK_HTTPURLCONNECTOR,
        NPC_CLIENT,
        NIMM_CLIENT
    ));

    private static List<ServiceType> VALUES;
    private static IntHashMap<ServiceType> CODE_LOOKUP_TABLE = null;
    private static Map<String, List<ServiceType>> STATISTICS_LOOKUP_TABLE = null;

    
    // Initialization
    static {
        ServiceTypeInitializer.checkServiceTypes(DEFAULT_VALUES);
        setValues(DEFAULT_VALUES);
    }

    private static void setValues(List<ServiceType> serviceTypes) {
        VALUES = serviceTypes;
        CODE_LOOKUP_TABLE = initializeServiceTypeCodeLookupTable(serviceTypes);
        STATISTICS_LOOKUP_TABLE = initializeServiceTypeStatisticsLookupTable(serviceTypes);
    }
    
    static synchronized boolean isInitialized() {
        return DEFAULT_VALUES != VALUES;
    }

    static synchronized void initialize(List<ServiceType> serviceTypes) {
        if (isInitialized()) {
            throw new IllegalStateException("ServiceType is already initialized");
        }

        setValues(serviceTypes);
    }

    private static Map<String, List<ServiceType>> initializeServiceTypeStatisticsLookupTable(List<ServiceType> serviceTypes) {
        final Map<String, List<ServiceType>> table = new HashMap<String, List<ServiceType>>();

        for (ServiceType serviceType : serviceTypes) {
            if (serviceType.isRecordStatistics()) {
                List<ServiceType> serviceTypeList = table.get(serviceType.getDesc());
                if (serviceTypeList == null) {
                    serviceTypeList = new ArrayList<ServiceType>();
                    table.put(serviceType.getDesc(), serviceTypeList);
                }
                serviceTypeList.add(serviceType);
            }
        }

        // value of this table will be exposed. so make them unmodifiable.
        final Map<String, List<ServiceType>> unmodifiable = new HashMap<String, List<ServiceType>>(table.size());

        for (Map.Entry<String, List<ServiceType>> entry : table.entrySet()) {
            List<ServiceType> newValue = Collections.unmodifiableList(entry.getValue());
            unmodifiable.put(entry.getKey(), newValue);
        }

        return unmodifiable;
    }

    private static IntHashMap<ServiceType> initializeServiceTypeCodeLookupTable(List<ServiceType> serviceTypes) {
        IntHashMap<ServiceType> table = new IntHashMap<ServiceType>(256);

        for (ServiceType serviceType : serviceTypes) {
            table.put(serviceType.getCode(), serviceType);
        }

        return table;
    }

    
    
    
    // FIXME it may be not good to find serviceType by using this api
    public static List<ServiceType> findDesc(String desc) {
        if (desc == null) {
            throw new NullPointerException("desc must not be null");
        }

        return STATISTICS_LOOKUP_TABLE.get(desc);
    }

    public static boolean isWas(final short code) {
        return ServiceTypeCategory.SERVER.contains(code);
    }

    public static ServiceType findServiceType(short code) {
        ServiceType serviceType = CODE_LOOKUP_TABLE.get(code);
        if (serviceType == null) {
            return UNDEFINED;
        }
        return serviceType;
    }

    public static List<ServiceType> values() {
        return VALUES;
    }

    public static ServiceType valueOf(String name) {
        for (ServiceType type : VALUES) {
            if (name.equals(type.name)) {
                return type;
            }
        }

        throw new NoSuchElementException(name);
    }
}
