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

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author koo.taejin
 */
public class TCommandRegistry implements TBaseLocator {

    private final ConcurrentHashMap<Short, TCommandType> commandTBaseRepository = new ConcurrentHashMap<Short, TCommandType>();

    public TCommandRegistry(TCommandTypeVersion version) {
        this(version.getSupportCommandList());
    }

    public TCommandRegistry(List<TCommandType> supportCommandList) {
        for (TCommandType type : supportCommandList) {
            commandTBaseRepository.put(type.getCode(), type);
        }
    }

    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        TCommandType commandTBaseType = commandTBaseRepository.get(type);
        if (commandTBaseType == null) {
            throw new TException("Unsupported type:" + type);
        }

        return commandTBaseType.newObject();
    }

    @Override
    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }

        // Should we preload commandTBaseList for performance? 
        Collection<TCommandType> commandTBaseList = commandTBaseRepository.values();

        for (TCommandType commandTBase : commandTBaseList) {
            if (commandTBase.isInstanceOf(tbase)) {
                return commandTBase.getHeader();
            }
        }
        
        throw new TException("Unsupported Type" + tbase.getClass());
    }

    @Override
    public boolean isSupport(short type) {
        TCommandType commandTBaseType = commandTBaseRepository.get(type);

        if (commandTBaseType != null) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isSupport(Class<? extends TBase> clazz) {
        // Should we preload commandTBaseList for performance? 
        Collection<TCommandType> commandTBaseList = commandTBaseRepository.values();

        for (TCommandType commandTBase : commandTBaseList) {
            if (commandTBase.getClazz().equals(clazz)) {
                return true;
            }
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
