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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import org.apache.thrift.TBase;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Taejin Koo
 */
public abstract class ThriftBoMapperTestBase<T extends TBase, P extends AgentStatDataPoint> {

    private static final int NUM_CREATE_OBJECT = 5;

    protected abstract T create();

    protected abstract P convert(T original);

    protected abstract void verify(T original, P mappedStatDataPoint);

    @Test
    public void mapperTest() {
        mapperTest0();
    }

    private void mapperTest0() {
        for (int i = 0; i < NUM_CREATE_OBJECT; i++) {
            T tBase = create();
            P mappedStatDataPoint = convert(tBase);
            verify(tBase, mappedStatDataPoint);
        }
    }

    static boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    static int getRandomInteger(int original, int bound) {
        return ThreadLocalRandom.current().nextInt(original, bound);
    }

    static long getRandomLong(long original, long bound) {
        return ThreadLocalRandom.current().nextLong(original, bound);
    }

    static double getRandomDouble(double original, double bound) {
        return ThreadLocalRandom.current().nextDouble(original, bound);
    }

}
