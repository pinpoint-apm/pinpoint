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

package com.navercorp.pinpoint.thrift.util;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TCompactProtocolTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void compactProtocolVint() throws TException {
        TMemoryBuffer tMemoryBuffer = writeVInt32(BytesUtils.zigzagToInt(64));
        logger.trace("length:{}", tMemoryBuffer.length());

        TMemoryBuffer tMemoryBuffer2 = writeVInt32(64);
        logger.trace("length:{}", tMemoryBuffer2.length());

    }

    private TMemoryBuffer writeVInt32(int i) throws TException {
        TMemoryBuffer tMemoryBuffer = new TMemoryBuffer(10);
        TCompactProtocol tCompactProtocol = new TCompactProtocol(tMemoryBuffer);
        tCompactProtocol.writeI32(i);
        return tMemoryBuffer;
    }
}
