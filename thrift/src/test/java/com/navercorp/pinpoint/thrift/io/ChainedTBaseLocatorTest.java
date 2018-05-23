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

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ChainedTBaseLocatorTest {

    private ChainedTBaseLocator chainedTBaseLocator;

    private final short tBase1Code = 10;
    private final TBase1 tBase1 = Mockito.mock(TBase1.class);

    private final short tBase2Code = 20;
    private final TBase2 tBase2 = Mockito.mock(TBase2.class);

    @Before
    public void setUp() throws Exception {
        TBaseLocator headLocator = new TestTBaseLocator(tBase1Code, tBase1);

        TestTBaseLocator testTBaseLocator = new TestTBaseLocator(tBase2Code, tBase2);

        ArrayList<TBaseLocator> locatorList = new ArrayList<TBaseLocator>();
        locatorList.add(testTBaseLocator);

        this.chainedTBaseLocator = new ChainedTBaseLocator(headLocator, locatorList);
    }

    @Test
    public void isSupportTest() {
        Assert.assertTrue(chainedTBaseLocator.isSupport(tBase1Code));
        Assert.assertTrue(chainedTBaseLocator.isSupport(tBase2Code));
        Assert.assertFalse(chainedTBaseLocator.isSupport((short) 30));
    }

    @Test
    public void isSupportTest2() {
        Assert.assertTrue(chainedTBaseLocator.isSupport(tBase1.getClass()));
        Assert.assertTrue(chainedTBaseLocator.isSupport(tBase2.getClass()));
        Assert.assertFalse(chainedTBaseLocator.isSupport(TResult.class));
    }

    @Test(expected = TException.class)
    public void headerLookupTest() throws TException {
        Assert.assertNotNull(chainedTBaseLocator.headerLookup(tBase1));
        Assert.assertNotNull(chainedTBaseLocator.headerLookup(tBase2));
        chainedTBaseLocator.headerLookup(new TResult());
    }

    @Test(expected = TException.class)
    public void tBaseLookupTest() throws TException {
        Assert.assertNotNull(chainedTBaseLocator.tBaseLookup(tBase1Code));
        Assert.assertNotNull(chainedTBaseLocator.tBaseLookup(tBase2Code));
        Assert.assertNotNull(chainedTBaseLocator.tBaseLookup((short)30));
    }

    private static interface TBase1 extends TBase {
    }

    private static interface TBase2 extends TBase {
    }

    private static class TestTBaseLocator implements TBaseLocator {

        private final short type;
        private final TBase<?, ?> tBase;

        public TestTBaseLocator(short type, TBase<?, ?> tBase) {
            this.type = type;
            this.tBase = tBase;
        }

        @Override
        public TBase<?, ?> tBaseLookup(short type) throws TException {
            if (this.type == type) {
                return tBase;
            }
            throw new TException("Unsupported type:" + type);
        }

        @Override
        public Header headerLookup(TBase<?, ?> tbase) throws TException {
            if (this.tBase.getClass().isInstance(tbase)) {
                Header header = Mockito.mock(Header.class);
                return header;
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
            if (clazz.equals(tBase.getClass())) {
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



}
