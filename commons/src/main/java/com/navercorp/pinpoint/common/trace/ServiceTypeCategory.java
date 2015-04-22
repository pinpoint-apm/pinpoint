/**
 * Copyright 2014 NAVER Corp.
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

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public enum ServiceTypeCategory {
    PINPOINT_INTERNAL((short)0, (short)999),
    SERVER((short)1000, (short)1999),
    RPC((short)9000, (short)9999);
   
    
    private final short minCode;
    private final short maxCode;
    
    private ServiceTypeCategory(short minCode, short maxCode) {
        this.minCode = minCode;
        this.maxCode = maxCode;
    }
    
    public boolean contains(short code) {
        return minCode <= code && code <= maxCode; 
    }
    
    public boolean contains(ServiceType type) {
        return contains(type.getCode());
    }
    
}
