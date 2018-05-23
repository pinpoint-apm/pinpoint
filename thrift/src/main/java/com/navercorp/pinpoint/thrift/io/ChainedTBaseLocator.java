/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.io.header.Header;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ChainedTBaseLocator implements TBaseLocator {

    private final TBaseLocator headLocator;
    private final List<TBaseLocator> locatorList;

    public ChainedTBaseLocator(TBaseLocator tBaseLocator) {
        this(tBaseLocator, null);
    }

    public ChainedTBaseLocator(TBaseLocator tBaseLocator, List<TBaseLocator> tBaseLocatorList) {
        this.headLocator = Assert.requireNonNull(tBaseLocator, "headLocator must not be null");

        if (CollectionUtils.isEmpty(tBaseLocatorList)) {
            this.locatorList = Collections.emptyList();
        } else {
            this.locatorList = Collections.unmodifiableList(tBaseLocatorList);
        }
    }

    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        if (headLocator.isSupport(type)) {
            return headLocator.tBaseLookup(type);
        }

        for (TBaseLocator locator : locatorList) {
            if (locator.isSupport(type)) {
                return locator.tBaseLookup(type);
            }
        }

        throw new TException("Unsupported type:" + type);
    }

    /*
     *   Based on DefaultTBaseLocator
     *   check if clazz is same in the method of isSupport,
     *   but, check if object can be instanceof in the method of headerLookup, .
     *   However, TBase is not inherited again in pinpoint.
     *   If the logic changes, this part needs to be corrected as well.
     */
    @Override
    public Header headerLookup(TBase<?, ?> dto) throws TException {
        Assert.requireNonNull(dto, "dto must not be null");

        if (headLocator.isSupport(dto.getClass())) {
            return headLocator.headerLookup(dto);
        }

        for (TBaseLocator locator : locatorList) {
            if (locator.isSupport(dto.getClass())) {
                return locator.headerLookup(dto);
            }
        }

        throw new TException("Unsupported Type" + dto.getClass());
    }

    @Override
    public boolean isSupport(short type) {
        if (headLocator.isSupport(type)) {
            return true;
        }

        for (TBaseLocator locator : locatorList) {
            if (locator.isSupport(type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isSupport(Class<? extends TBase> clazz) {
        if (headLocator.isSupport(clazz)) {
            return true;
        }

        for (TBaseLocator locator : locatorList) {
            if (locator.isSupport(clazz)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Header getChunkHeader() {
        Header chunkHeader = headLocator.getChunkHeader();
        if (chunkHeader != null) {
            return chunkHeader;
        }

        for (TBaseLocator locator : locatorList) {
            chunkHeader = locator.getChunkHeader();
            if (chunkHeader != null) {
                return chunkHeader;
            }
        }

        return null;
    }

    @Override
    public boolean isChunkHeader(short type) {
        if (headLocator.isChunkHeader(type)) {
            return true;
        }

        for (TBaseLocator locator : locatorList) {
            if (locator.isChunkHeader(type)) {
                return true;
            }
        }

        return false;
    }

}
