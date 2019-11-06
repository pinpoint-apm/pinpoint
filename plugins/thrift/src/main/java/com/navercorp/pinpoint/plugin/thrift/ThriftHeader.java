/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift;

import org.apache.thrift.protocol.TType;

import java.util.EnumSet;
import java.util.Set;

public enum ThriftHeader {
    THRIFT_TRACE_ID                (TType.STRING,  (short) Short.MIN_VALUE),
    THRIFT_SPAN_ID                 (TType.I64,     (short)(Short.MIN_VALUE+1)),
    THRIFT_PARENT_SPAN_ID          (TType.I64,     (short)(Short.MIN_VALUE+2)),
    THRFIT_SAMPLED                 (TType.BOOL,    (short)(Short.MIN_VALUE+3)),
    THRIFT_FLAGS                   (TType.I16,     (short)(Short.MIN_VALUE+4)),
    THRIFT_PARENT_APPLICATION_NAME (TType.STRING,  (short)(Short.MIN_VALUE+5)),
    THRIFT_PARENT_APPLICATION_TYPE (TType.I16,     (short)(Short.MIN_VALUE+6)),
    THRIFT_HOST                    (TType.STRING,  (short)(Short.MIN_VALUE+7));

    private final short id;
    
    private final byte type;

    private static final Set<ThriftHeader> HEADERS = EnumSet.allOf(ThriftHeader.class);
    
    ThriftHeader(byte type, short id) {
        this.type = type;
        this.id = id;
    }
    
    public short getId() {
        return this.id;
    }
    
    public byte getType() {
        return this.type;
    }
    
    /**
     * Returns the {@link ThriftRequestProperty} with the specified id,
     * or {@code null} if there is none.
     *
     * @param id the id of the associated <tt>ThriftHeaderKey</tt>
     * @return the <tt>ThriftHeaderKey</tt> associated with the specified id, or
     *     <tt>null</tt> if there is none
     */
    public static ThriftHeader findThriftHeaderKeyById(short id) {
        for (ThriftHeader headerKey : HEADERS) {
            if (headerKey.id == id) {
                return headerKey;
            }
        }
        return null;
    }
    
}