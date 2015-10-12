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
 * 
 * 
 * <h3>Pinpoint Internal (0 ~ 999)</h3>
 * 
 * <table>
 * <tr><td>-1</td><td>UNDEFINED</td></tr>
 * <tr><td>1</td><td>UNKNOWN</td></tr>
 * <tr><td>2</td><td>USER</td></tr>
 * <tr><td>3</td><td>UNKNOWN_GROUP</td></tr>
 * <tr><td>5</td><td>TEST</td></tr>
 * <tr><td>7</td><td>COLLECTOR</td></tr>
 * <tr><td>100</td><td>ASYNC</td></tr>
 * </table>
 *
 * 
 * <h3>Server (1000 ~ 1899)</h3>
 * 
 * <table>
 * <tr><td>1000</td><td>STAND_ALONE</td></tr>
 * <tr><td>1005</td><td>TEST_STAND_ALONE</td></tr>
 * <tr><td>1010</td><td>TOMCAT</td></tr>
 * <tr><td>1011</td><td>TOMCAT_METHOD</td></tr>
 * <tr><td>1020</td><td><i>RESERVED</i></td></tr>
 * <tr><td>1021</td><td><i>RESERVED</i></td></tr>
 * <tr><td>1030</td><td>JETTY</td></tr>
 * <tr><td>1031</td><td>JETTY_METHOD</td></tr>
 * <tr><td>1100</td><td>THRIFT_SERVER</td></tr>
 * <tr><td>1101</td><td>THRIFT_SERVER_INTERNAL</td></tr>
 * </table>
 * 
 * <h3>Server Sandbox (1900 ~ 1999)</h3>
 * 
 * 
 * <h3>Database (2000 ~ 2899)</h3>
 * <table>
 * <tr><td>2050</td><td>UNKNOWN_DB</td></tr>
 * <tr><td>2051</td><td>UNKNOWN_DB_EXECUTE_QUERY</td></tr>
 * <tr><td>2100</td><td>MYSQL</td></tr>
 * <tr><td>2101</td><td>MYSQL_EXECUTE_QUERY</td></tr>
 * <tr><td>2200</td><td>MSSQL</td></tr>
 * <tr><td>2201</td><td>MSSQL_EXECUTE_QUERY</td></tr>
 * <tr><td>2300</td><td>ORACLE</td></tr>
 * <tr><td>2301</td><td>ORACLE_EXECUTE_QUERY</td></tr>
 * <tr><td>2400</td><td>CUBRID</td></tr>
 * <tr><td>2401</td><td>CUBRID_EXECUTE_QUERY</td></tr>
 * </table>
 *
 * <h3>Database Sandbox (2900 ~ 2999)</h3>
 *
 *
 * <h3>RESERVED (3000 ~ 4999)</h3>
 *
 *
 * <h3>Library (5000 ~ 7499)</h3>
 * <table>
 * <tr><td>5000</td><td>INTERNAL_METHOD</td></tr>
 * <tr><td>5010</td><td>GSON</td></tr>
 * <tr><td>5011</td><td>JACKSON</td></tr>
 * <tr><td>5012</td><td>JSON-LIB</td></tr>
 * <tr><td>5050</td><td>SPRING</td></tr>
 * <tr><td>5051</td><td>SPRING_MVC</td></tr>
 * <tr><td>5061</td><td><i>RESERVED</i></td></tr>
 * <tr><td>5071</td><td>SPRING_BEAN</td></tr>
 * <tr><td>5500</td><td>IBATIS</td></tr>
 * <tr><td>5501</td><td>IBATIS-SPRING</td></tr>
 * <tr><td>5510</td><td>MYBATIS</td></tr>
 * <tr><td>6050</td><td>DBCP</td></tr>
 * <tr><td>7010</td><td>USER_INCLUDE</td></tr>
 * </table>
 *
 * <h3>Library Sandbox (7500 ~ 7999)</h3>
 *
 * <h3>Cache Library (8000 ~ 8899) Fast Histogram</h3>
 * <table>
 * <tr><td>8050</td><td>MEMCACHED</td></tr>
 * <tr><td>8051</td><td>MEMCACHED_FUTURE_GET</td></tr>
 * <tr><td>8100</td><td>ARCUS</td></tr>
 * <tr><td>8101</td><td>ARCUS_FUTURE_GET</td></tr>
 * <tr><td>8102</td><td>ARCUS_EHCACHE_FUTURE_GET</td></tr>
 * <tr><td>8103</td><td>ARCUS_INTERNAL</td></tr>
 * <tr><td>8200</td><td>REDIS</td></tr>
 * <tr><td>8250</td><td><i>RESERVED</i></td></tr>
 * <tr><td>8251</td><td><i>RESERVED</i></td></tr>
 * </table>
 * <h3>Cache Library Sandbox (8900 ~ 8999) Histogram type: Fast </h3>
 * 
 * 
 * <h3>RPC (9000 ~ 9899)</h3>
 * <table>
 * <tr><td>9050</td><td>HTTP_CLIENT_3</td></tr>
 * <tr><td>9051</td><td>HTTP_CLIENT_3_INTERNAL</td></tr>
 * <tr><td>9052</td><td>HTTP_CLIENT_4</td></tr>
 * <tr><td>9053</td><td>HTTP_CLIENT_4_INTERNAL</td></tr>
 * <tr><td>9054</td><td>GOOGLE_HTTP_CLIENT_INTERNAL</td></tr>
 * <tr><td>9055</td><td>JDK_HTTPURLCONNECTOR</td></tr>
 * <tr><td>9056</td><td>ASYNC_HTTP_CLIENT</td></tr>
 * <tr><td>9057</td><td>ASYNC_HTTP_CLIENT_INTERNAL</td></tr>
 * <tr><td>9058</td><td>OK_HTTP_CLIENT</td></tr>
 * <tr><td>9059</td><td>OK_HTTP_CLIENT_INTERNAL</td></tr>
 * <tr><td>9060</td><td><i>RESERVED</i></td></tr>
 * <tr><td>9070</td><td><i>RESERVED</i></td></tr>
 * <tr><td>9100</td><td>THRIFT_CLIENT</td></tr>
 * <tr><td>9101</td><td>THRIFT_CLIENT_INTERNAL</td></tr>
 * </table>
 * 
 * <h3>RPC Sandbox (9900 ~ 9999)</h3>
 *
 * 
 * <tr><td></td><td></td></tr>
 * 
 * @author emeroad
 * @author netspider
 * @author Jongho Moon
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
    private final ServiceTypeCategory category;

    public static ServiceType of(int code, String name, ServiceTypeProperty... properties) {
        return of(code, name, name, properties);
    }

    public static ServiceType of(int code, String name, String desc, ServiceTypeProperty... properties) {
        return new ServiceType(code, name, desc, properties);
    }

    ServiceType(int code, String name, String desc, ServiceTypeProperty... properties) {
        // code must be a short value but constructors accept int to make declaring ServiceType values more cleaner by removing casting to short.
        if (code > Short.MAX_VALUE || code < Short.MIN_VALUE) {
            throw new IllegalArgumentException("code must be a short value");
        }

        this.code = (short)code;
        this.name = name;
        this.desc = desc;

        this.category = ServiceTypeCategory.findCategory((short)code);

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

    public ServiceTypeCategory getCategory() {
        return category;
    }

    public HistogramSchema getHistogramSchema() {
        return category.getHistogramSchema();
    }

    public boolean isWas() {
        return this.category == ServiceTypeCategory.SERVER;
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
        
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        } else if (!category.equals(other.category)) {
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

    public static boolean isWas(final short code) {
        return ServiceTypeCategory.SERVER.contains(code);
    }


    // Undefined Service Code
    public static final ServiceType UNDEFINED = of(-1, "UNDEFINED", TERMINAL);

    // Callee node that agent hasn't been installed
    public static final ServiceType UNKNOWN = of(1, "UNKNOWN", RECORD_STATISTICS);

    // UserUNDEFINED
    public static final ServiceType USER = of(2, "USER", RECORD_STATISTICS);

    // Group of UNKNOWN, used only for UI
    public static final ServiceType UNKNOWN_GROUP = of(3, "UNKNOWN_GROUP", RECORD_STATISTICS);

    // Group of TEST, used for running tests
    public static final ServiceType TEST = of(5, "TEST");

    public static final ServiceType COLLECTOR = of(7, "COLLECTOR");
    
    public static final ServiceType ASYNC = of(100, "ASYNC");
    
    // Java applications, WAS
    public static final ServiceType STAND_ALONE = of(1000, "STAND_ALONE", RECORD_STATISTICS);
    public static final ServiceType TEST_STAND_ALONE = of(1005, "TEST_STAND_ALONE", RECORD_STATISTICS);


    /**
     * Database shown only as xxx_EXECUTE_QUERY at the statistics info section in the server map
     */
    // DB 2000
    public static final ServiceType UNKNOWN_DB = of(2050, "UNKNOWN_DB", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType UNKNOWN_DB_EXECUTE_QUERY = of(2051, "UNKNOWN_DB_EXECUTE_QUERY", "UNKNOWN_DB", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);

    // Internal method
    // FIXME it's not clear to put internal method here. but do that for now.
    public static final ServiceType INTERNAL_METHOD = of(5000, "INTERNAL_METHOD");
    

    // Spring framework
    public static final ServiceType SPRING = of(5050, "SPRING");
//    public static final ServiceType SPRING_MVC = of(5051, "SPRING_MVC", "SPRING", NORMAL_SCHEMA);
    // FIXME replaced with IBATIS_SPRING (5501) under IBatis Plugin - kept for backwards compatibility
    public static final ServiceType SPRING_ORM_IBATIS = of(5061, "SPRING_ORM_IBATIS", "SPRING");
    // FIXME need to define how to handle spring related codes
//    public static final ServiceType SPRING_BEAN = of(5071, "SPRING_BEAN", "SPRING_BEAN", NORMAL_SCHEMA);
}
