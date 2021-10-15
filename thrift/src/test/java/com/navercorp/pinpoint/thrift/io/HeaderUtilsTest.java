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
import com.navercorp.pinpoint.io.header.InvalidHeaderException;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class HeaderUtilsTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Test
    public void validateSignature() throws TException {
        Header header = new HeaderV1((short) 1);
        Assert.assertEquals(HeaderUtils.validateSignature(header.getSignature()), HeaderUtils.OK);

        logger.debug(header.toString());
    }

    @Test(expected = InvalidHeaderException.class)
    public void validateSignature_error() throws TException {
        Header error = new HeaderV1((byte) 0x11, (byte) 0x20, (short) 1);
        Assert.assertNotEquals(HeaderUtils.validateSignature(error.getSignature()), HeaderUtils.OK);


        logger.debug(error.toString());
    }

}
