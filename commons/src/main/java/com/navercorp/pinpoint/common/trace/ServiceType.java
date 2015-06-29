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

package com.navercorp.pinpoint.common.trace;

import static com.navercorp.pinpoint.common.trace.HistogramSchema.*;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

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
    private final boolean terminal;

    // FIXME record statistics of only rpc call currently. so is it all right to chane into isRecordRpc()
    private final boolean recordStatistics;

    // whether or not print out api including destinationId
    private final boolean includeDestinationId;
    private final HistogramSchema histogramSchema;
    
    public static ServiceType of(int code, String name, HistogramSchema histogramSchema, ServiceTypeProperty... properties) {
        return of(code, name, name, histogramSchema, properties);
    }

    public static ServiceType of(int code, String name, String desc, HistogramSchema histogramSchema, ServiceTypeProperty... properties) {
        return new ServiceType(code, name, desc, histogramSchema, properties);
    }


    public ServiceType(int code, String name, String desc, HistogramSchema histogramSchema, ServiceTypeProperty... properties) {
        // code must be a short value but constructors accept int to make declaring ServiceType values more cleaner by removing casting to short.
        if (code > Short.MAX_VALUE || code < Short.MIN_VALUE) {
            throw new IllegalArgumentException("code must be a short value");
        }
        checkSupportHistogramSchema(code, histogramSchema);
        this.code = (short)code;
        this.name = name;
        this.desc = desc;

        this.histogramSchema = histogramSchema;

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
            default:
                throw new IllegalStateException("Unknown ServiceTypeProperty:" + property);
            }
        }
        
        this.terminal = terminal;
        this.recordStatistics = recordStatistics;
        this.includeDestinationId = includeDestinationId;
    }

    private void checkSupportHistogramSchema(int code, HistogramSchema histogramSchema) {
        if (!isWas((short)code)) {
            return;
        }
        if (histogramSchema != HistogramSchema.NORMAL_SCHEMA) {
            throw new IllegalArgumentException("Server ServiceType only support HistogramSchema.NORMAL_SCHEMA. code:" + code);
        }
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
    
    @Override
    public String toString() {
        return desc;
    }

    @Override
    public int hashCode() {
        // ServiceType's hashCode method is not used as they are put into IntHashMap (see ServiceTypeRegistry)
        // which uses ServiceType code as key. It shouldn't really matter what this method returns.
        final int prime = 31;
        int result = 1;
        result = prime * result + code;
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        ServiceType other = (ServiceType) obj;
        if (code != other.code) {
            return false;
        }
        if (desc == null) {
            if (other.desc != null) {
                return false;
            }
        } else if (!desc.equals(other.desc)) {
            return false;
            
        }
        
        if (histogramSchema == null) {
            if (other.histogramSchema != null) {
                return false;
            }
        } else if (!histogramSchema.equals(other.histogramSchema)) {
            return false;
        }
        
        if (includeDestinationId != other.includeDestinationId) {
            return false;
        }
        
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        
        if (recordStatistics != other.recordStatistics) {
            return false;
        }
        
        if (terminal != other.terminal) {
            return false;
        }
        
        return true;
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

    public static final ServiceType COLLECTOR = of(7, "COLLECTOR", NORMAL_SCHEMA);
    
    public static final ServiceType ASYNC = of(100, "ASYNC", NORMAL_SCHEMA);
    
    // Java applications, WAS
    public static final ServiceType STAND_ALONE = of(1000, "STAND_ALONE", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType TEST_STAND_ALONE = of(1005, "TEST_STAND_ALONE", NORMAL_SCHEMA, RECORD_STATISTICS);

    /**
     * Database shown only as xxx_EXECUTE_QUERY at the statistics info section in the server map
     */
    // DB 2000
    public static final ServiceType UNKNOWN_DB = of(2050, "UNKNOWN_DB", NORMAL_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType UNKNOWN_DB_EXECUTE_QUERY = of(2051, "UNKNOWN_DB_EXECUTE_QUERY", "UNKNOWN_DB", NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final ServiceType MYSQL = of(2100, "MYSQL", NORMAL_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType MYSQL_EXECUTE_QUERY = of(2101, "MYSQL_EXECUTE_QUERY", "MYSQL", NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final ServiceType MSSQL = of(2200, "MSSQL", "MSSQLSERVER", NORMAL_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType MSSQL_EXECUTE_QUERY = of(2201, "MSSQL_EXECUTE_QUERY", "MSSQLSERVER", NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    public static final ServiceType ORACLE = of(2300, "ORACLE", NORMAL_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ORACLE_EXECUTE_QUERY = of(2301, "ORACLE_EXECUTE_QUERY", "ORACLE", NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

//    public static final ServiceType CUBRID = of(2400, "CUBRID", NORMAL_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
//    public static final ServiceType CUBRID_EXECUTE_QUERY = of(2401, "CUBRID_EXECUTE_QUERY", "CUBRID", NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    // Internal method
    // FIXME it's not clear to put internal method here. but do that for now.
    public static final ServiceType INTERNAL_METHOD = of(5000, "INTERNAL_METHOD", NORMAL_SCHEMA);
    
    // 5010 gson
    // 5011 jackson
    // 5012 json-lib

    // Spring framework
    public static final ServiceType SPRING = of(5050, "SPRING", NORMAL_SCHEMA);
    public static final ServiceType SPRING_MVC = of(5051, "SPRING_MVC", "SPRING", NORMAL_SCHEMA);
    // FIXME need to define how to handle spring related codes
    public static final ServiceType SPRING_ORM_IBATIS = of(5061, "SPRING_ORM_IBATIS", "SPRING", NORMAL_SCHEMA);
    public static final ServiceType SPRING_BEAN = of(5071, "SPRING_BEAN", "SPRING_BEAN", NORMAL_SCHEMA);

    // xBatis
    public static final ServiceType IBATIS = of(5500, "IBATIS", NORMAL_SCHEMA);
    public static final ServiceType MYBATIS = of(5510, "MYBATIS", NORMAL_SCHEMA);

    // DBCP
    public static final ServiceType DBCP = of(6050, "DBCP", NORMAL_SCHEMA);

    // USER INCLUDE
    public static final ServiceType USER_INCLUDE = of(7010, "USER_INCLUDE", NORMAL_SCHEMA);

    public static final ServiceType MEMCACHED = of(8050, "MEMCACHED", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS);
    public static final ServiceType MEMCACHED_FUTURE_GET = of(8051, "MEMCACHED_FUTURE_GET", "MEMCACHED", FAST_SCHEMA, TERMINAL);

    // Redis
    // public static final ServiceType REDIS = of(8200, "REDIS", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS);

    // Connector, Client
    public static final ServiceType HTTP_CLIENT = of(9050, "HTTP_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType HTTP_CLIENT_INTERNAL = of(9051, "HTTP_CLIENT_INTERNAL", "HTTP_CLIENT", NORMAL_SCHEMA);
//    public static final ServiceType JDK_HTTPURLCONNECTOR = of(9055, "JDK_HTTPURLCONNECTOR", "JDK_HTTPCONNECTOR", NORMAL_SCHEMA);
//    public static final ServiceType NPC_CLIENT = of(9060, "NPC_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType NIMM_CLIENT = of(9070, "NIMM_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);
    


    public static boolean isWas(final short code) {
        return ServiceTypeCategory.SERVER.contains(code);
    }
}
