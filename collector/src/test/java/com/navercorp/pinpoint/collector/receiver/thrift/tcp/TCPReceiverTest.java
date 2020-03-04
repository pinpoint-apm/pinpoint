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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author emeroad
 */
public class TCPReceiverTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void l4ip() throws UnknownHostException {
        InetAddress byName = InetAddress.getByName("10.12.13.10");
        logger.debug("byName:{}", byName);
    }

    @Test
    public void l4ipList() throws UnknownHostException {
        String two = "10.12.13.10,10.12.13.20";
        String[] split = two.split(",");
        Assert.assertEquals(split.length, 2);

        String twoEmpty = "10.12.13.10,";
        String[] splitEmpty = twoEmpty.split(",");
        Assert.assertEquals(splitEmpty.length, 1);

    }

}
