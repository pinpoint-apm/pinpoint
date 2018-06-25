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

import com.navercorp.pinpoint.io.header.Header;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author koo.taejin
 */
public class TCommandRegistry implements TBaseLocator {

    private final Map<Short, TCommandType> commandTBaseRepository;
    private final Set<TCommandType> typeSet;
    private final Set<Class<? extends TBase>> classSet;

    public TCommandRegistry(TCommandTypeVersion version) {
        this(version.getSupportCommandList());
    }

    public TCommandRegistry(List<TCommandType> supportCommandList) {
        if (supportCommandList == null) {
            throw new NullPointerException("supportCommandList must not be null");
        }
        this.commandTBaseRepository = toCodeMap(supportCommandList);
        // pre-build index
        this.typeSet = toTypeSet(supportCommandList);
        this.classSet = toClazzSet(supportCommandList);
    }


    private Set<Class<? extends TBase>> toClazzSet(List<TCommandType> supportCommandList) {
        if (supportCommandList.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Class<? extends TBase>> set = new HashSet<Class<? extends TBase>>();
        for (TCommandType tCommandType : supportCommandList) {
            set.add(tCommandType.getClazz());
        }
        return set;
    }

    private Map<Short, TCommandType> toCodeMap(List<TCommandType> supportCommandList) {
        final Map<Short, TCommandType> result = new HashMap<Short, TCommandType>();
        for (TCommandType type : supportCommandList) {
            result.put(type.getCode(), type);
        }
        return result;
    }

    private Set<TCommandType> toTypeSet(List<TCommandType> supportCommandList) {
        if (supportCommandList.isEmpty()) {
            return Collections.emptySet();
        }
        return EnumSet.copyOf(supportCommandList);
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

        final TCommandType tCommandType = findInstanceOf(tbase);
        if (tCommandType == null) {
            throw new TException("Unsupported Type" + tbase.getClass());
        }

        return tCommandType.getHeader();
    }

    private TCommandType findInstanceOf(TBase<?, ?> tbase) {
        for (TCommandType commandTBase : typeSet) {
            if (commandTBase.isInstanceOf(tbase)) {
                return commandTBase;
            }
        }
        return null;
    }

    @Override
    public boolean isSupport(short type) {
        final TCommandType commandTBaseType = commandTBaseRepository.get(type);
        if (commandTBaseType != null) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isSupport(Class<? extends TBase> clazz) {
        // Inheritance not support
        return classSet.contains(clazz);

        // Inheritance support version
//        for (TCommandType commandTBase : typeSet) {
//            final Class<? extends TBase> commandTBaseClass = commandTBase.getClazz();
//            if (commandTBaseClass.isAssignableFrom(clazz)) {
//                return true;
//            }
//        }
//
//        return false;
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
