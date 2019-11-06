/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.util.BodyFactory;
import com.navercorp.pinpoint.thrift.dto.command.*;
import org.apache.thrift.TBase;

import com.navercorp.pinpoint.thrift.dto.TResult;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author koo.taejin
 */
public enum TCommandType {

    // Using reflection would make code cleaner.
    // But it also makes it hard to handle exception, constructor and will show relatively low performance.

    RESULT((short) 320, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TResult();
        }
    }),

    TRANSFER((short) 700, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCommandTransfer();
        }
    }),
    TRANSFER_RESPONSE((short) 701, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCommandTransferResponse();
        }
    }),

    ECHO((short) 710, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCommandEcho();
        }
    }),

    THREAD_DUMP((short) 720, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCommandThreadDump();
        }
    }),
    THREAD_DUMP_RESPONSE((short) 721, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCommandThreadDumpResponse();
        }
    }),

    ACTIVE_THREAD_COUNT((short) 730, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCmdActiveThreadCount();
        }
    }),
    ACTIVE_THREAD_COUNT_RESPONSE((short) 731, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCmdActiveThreadCountRes();
        }
    }),

    ACTIVE_THREAD_DUMP((short) 740, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCmdActiveThreadDump();
        }
    }),
    ACTIVE_THREAD_DUMP_RESPONSE((short) 741, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCmdActiveThreadDumpRes();
        }
    }),

    ACTIVE_THREAD_LIGHT_DUMP((short) 750, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCmdActiveThreadLightDump();
        }
    }),
    ACTIVE_THREAD_LIGHT_DUMP_RESPONSE((short) 751, new BodyFactory<TBase<?, ?>>() {
        @Override
        public TBase<?, ?> getObject() {
            return new TCmdActiveThreadLightDumpRes();
        }
    });

    private final short code;
    private final Class<? extends TBase> clazz;
    private final BodyFactory<TBase<?, ?>> bodyFactory;

    private static final Set<TCommandType> TCOMMAND_TYPES = EnumSet.allOf(TCommandType.class);


    TCommandType(short code, BodyFactory<TBase<?, ?>> bodyFactory) {
        this.code = code;
        this.bodyFactory = bodyFactory;
        this.clazz = bodyFactory.getObject().getClass();
    }

    public short getCode() {
        return code;
    }

    public Class<? extends TBase> getClazz() {
        return clazz;
    }


    public BodyFactory<TBase<?, ?>> getBodyFactory() {
        return bodyFactory;
    }

    @Deprecated
    public static TCommandType getType(Class<? extends TBase> clazz) {
        for (TCommandType commandType : TCOMMAND_TYPES) {
            if (commandType.getClazz() == clazz) {
                return commandType;
            }
        }

        return null;
    }

    public static TCommandType getType(short code) {
        for (TCommandType commandType : TCOMMAND_TYPES) {
            if (commandType.getCode() == code) {
                return commandType;
            }
        }

        return null;
    }

}
