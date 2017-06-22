/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

/**
 * @author Taejin Koo
 */
public class AgentEventTBaseLocator implements TBaseLocator {

    private static final short DEADLOCK = 910;
    private static final Header DEADLOCK_HEADER = createHeader(DEADLOCK);

    private static Header createHeader(short type) {
        Header header = new Header();
        header.setType(type);
        return header;
    }

    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
            case DEADLOCK:
                return new TDeadlock();
        }
        throw new TException("Unsupported type:" + type);
    }

    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }
        if (tbase instanceof TDeadlock) {
            return DEADLOCK_HEADER;
        }

        throw new TException("Unsupported Type" + tbase.getClass());
    }

    @Override
    public boolean isSupport(short type) {
        try {
            tBaseLookup(type);
            return true;
        } catch (TException ignore) {
            // skip
        }

        return false;
    }

    @Override
    public boolean isSupport(Class<? extends TBase> clazz) {
        if (clazz.equals(TDeadlock.class)) {
            return true;
        }

        return false;
    }

    @Override
    public Header getChunkHeader() {
        return null;
    }

    @Override
    public boolean isChunkHeader(short type) {
        return false;
    }

}
