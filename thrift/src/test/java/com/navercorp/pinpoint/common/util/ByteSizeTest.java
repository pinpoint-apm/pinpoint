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

package com.navercorp.pinpoint.common.util;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ByteSizeTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void test() throws TException {
        TCompactProtocol.Factory factory = new TCompactProtocol.Factory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
        TIOStreamTransport transport = new TIOStreamTransport(baos);
        TProtocol protocol = factory.getProtocol(transport);

        long l = TimeUnit.DAYS.toMillis(1);
        logger.debug("day:{}", l);
        long currentTime = System.currentTimeMillis();
        logger.debug("currentTime:{}" + currentTime);
        protocol.writeI64(l);
        byte[] buffer = baos.toByteArray();
        logger.debug("{}", buffer.length);

    }


}
