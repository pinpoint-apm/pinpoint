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

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.thrift.dto.command.*;
import org.apache.thrift.TBase;

import com.navercorp.pinpoint.thrift.dto.TResult;

/**
 * @author koo.taejin
 */
public enum TCommandType {

    // Using reflection would make code cleaner.
    // But it also makes it hard to handle exception, constructor and will show relatively low performance.

    RESULT((short) 320, TResult.class) {
        @Override
        public TBase newObject() {
            return new TResult();
        }
    },
    TRANSFER((short) 700, TCommandTransfer.class) {
        @Override
        public TBase newObject() {
            return new TCommandTransfer();
        }
    },
    TRANSFER_RESPONSE((short) 701, TCommandTransferResponse.class) {
        @Override
        public TBase newObject() {
            return new TCommandTransferResponse();
        }
    },
    ECHO((short) 710, TCommandEcho.class) {
        @Override
        public TBase newObject() {
            return new TCommandEcho();
        }
    },
    THREAD_DUMP((short) 720, TCommandThreadDump.class) {
        @Override
        public TBase newObject() {
            return new TCommandThreadDump();
        }
    },
    THREAD_DUMP_RESPONSE((short) 721, TCommandThreadDumpResponse.class) {
        @Override
        public TBase newObject() {
            return new TCommandThreadDumpResponse();
        }
    },
    ACTIVE_THREAD_COUNT((short) 730, TCmdActiveThreadCount.class) {
        @Override
        public TBase newObject() {
            return new TCmdActiveThreadCount();
        }
    },
    ACTIVE_THREAD_COUNT_RESPONSE((short) 731, TCmdActiveThreadCountRes.class) {
        @Override
        public TBase newObject() {
            return new TCmdActiveThreadCountRes();
        }
    },
    ACTIVE_THREAD_DUMP((short) 740, TCmdActiveThreadDump.class) {
        @Override
        public TBase newObject() {
            return new TCmdActiveThreadDump();
        }
    },
    ACTIVE_THREAD_DUMP_RESPONSE((short) 741, TCmdActiveThreadDumpRes.class) {
        @Override
        public TBase newObject() {
            return new TCmdActiveThreadDumpRes();
        }
    };

    private final short type;
    private final Class<? extends TBase> clazz;
    private final Header header;

    private TCommandType(short type, Class<? extends TBase> clazz) {
        this.type = type;
        this.clazz = clazz;
        this.header = createHeader(type);
    }

    protected short getType() {
        return type;
    }

    protected Class getClazz() {
        return clazz;
    }

    protected boolean isInstanceOf(Object value) {
        return this.clazz.isInstance(value);
    }

    protected Header getHeader() {
        return header;
    }

    public abstract TBase newObject();

    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
    }

}
