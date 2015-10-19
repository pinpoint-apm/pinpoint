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


class DefaultServiceType implements ServiceType {
    private final short code;
    private final String name;
    private final String desc;
    private final boolean terminal;

    // FIXME record statistics of only rpc call currently. so is it all right to chane into isRecordRpc()
    private final boolean recordStatistics;

    // whether or not print out api including destinationId
    private final boolean includeDestinationId;
    private final ServiceTypeCategory category;



    DefaultServiceType(int code, String name, String desc, ServiceTypeProperty... properties) {
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


    @Override
    public boolean isInternalMethod() {
        return this == INTERNAL_METHOD;
    }

    @Override
    public boolean isRpcClient() {
        return ServiceTypeCategory.RPC.contains(code);
    }

    // FIXME record statistics of only rpc call currently. so is it all right to chane into isRecordRpc()
    @Override
    public boolean isRecordStatistics() {
        return recordStatistics;
    }

    @Override
    public boolean isUnknown() {
        return this == ServiceType.UNKNOWN; // || this == ServiceType.UNKNOWN_CLOUD;
    }

    // return true when the service type is USER or can not be identified
    @Override
    public boolean isUser() {
        return this == ServiceType.USER;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public short getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public boolean isIncludeDestinationId() {
        return includeDestinationId;
    }

    @Override
    public ServiceTypeCategory getCategory() {
        return category;
    }

    @Override
    public HistogramSchema getHistogramSchema() {
        return category.getHistogramSchema();
    }

    @Override
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
        
        DefaultServiceType other = (DefaultServiceType) obj;
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


    // FIXME need to define how to handle spring related codes
//    ServiceType SPRING_BEAN = of(5071, "SPRING_BEAN", "SPRING_BEAN", NORMAL_SCHEMA);
}
