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

package com.navercorp.pinpoint.rpc.util;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author koo.taejin
 */
public class IDGenerator {

    private final AtomicInteger idGenerator;

    private final int gap;

    public IDGenerator() {
        this(1, 1);
    }

    public IDGenerator(int startIndex) {
        this(startIndex, 1);
    }

    public IDGenerator(int startIndex, int gap) {
        Assert.isTrue(startIndex >= 0, "StartIndex must be grater than or equal to 0.");
        Assert.isTrue(gap > 0, "Gap must be grater than 0.");

        this.gap = gap;

        this.idGenerator = new AtomicInteger(startIndex);
    }

    public int generate() {
        return idGenerator.getAndAdd(gap);
    }

    public int get() {
        return idGenerator.get();
    }

    public static IDGenerator createOddIdGenerator() {
        return new IDGenerator(1, 2);
    }

    public static IDGenerator createEvenIdGenerator() {
        return new IDGenerator(2, 2);
    }

}
