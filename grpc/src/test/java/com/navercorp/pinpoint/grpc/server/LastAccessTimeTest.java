/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.server;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LastAccessTimeTest {

    @Test
    public void update() {
        LastAccessTime lastAccessTime = new LastAccessTime(1);
        Assert.assertTrue(lastAccessTime.update(2));
    }

    @Test
    public void update_fail() {
        LastAccessTime lastAccessTime = new LastAccessTime(1);
        Assert.assertFalse(lastAccessTime.update(0));
    }

    @Test
    public void expire() {
        LastAccessTime lastAccessTime = new LastAccessTime(1);
        Assert.assertTrue(lastAccessTime.expire(2));

    }

    @Test
    public void expire_fail() {
        LastAccessTime lastAccessTime = new LastAccessTime(1);
        Assert.assertFalse(lastAccessTime.expire(0));
    }

    @Test
    public void expire_after() {
        LastAccessTime lastAccessTime = new LastAccessTime(1);
        Assert.assertTrue(lastAccessTime.expire(2));

        Assert.assertFalse(lastAccessTime.expire(3));
    }

    @Test
    public void isExpire() {
    }


}