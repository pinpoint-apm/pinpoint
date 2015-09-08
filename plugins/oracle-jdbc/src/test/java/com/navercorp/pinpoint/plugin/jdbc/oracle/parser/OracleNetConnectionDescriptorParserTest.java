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

package com.navercorp.pinpoint.plugin.jdbc.oracle.parser;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.plugin.jdbc.oracle.parser.Description;
import com.navercorp.pinpoint.plugin.jdbc.oracle.parser.KeyValue;
import com.navercorp.pinpoint.plugin.jdbc.oracle.parser.OracleConnectionStringException;
import com.navercorp.pinpoint.plugin.jdbc.oracle.parser.OracleNetConnectionDescriptorParser;


/**
 * @author emeroad
 */
public class OracleNetConnectionDescriptorParserTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void simple() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)"
                                        + "(CONNECT_DATA=(SERVICE_NAME=service)))";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        KeyValue keyValue = parser.parse();

        logger.info(keyValue.toString());

        Description des = new Description(keyValue);
        Assert.assertEquals(des.getServiceName(), "service");
    }

    @Test
    public void emptyValue() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=)"
                                        + "(CONNECT_DATA=(SERVICE_NAME=)))";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        KeyValue keyValue = parser.parse();
        logger.info(keyValue.toString());

        Description des = new Description(keyValue);
        Assert.assertEquals(des.getServiceName(), null);

    }

    @Test
    public void parse() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1522))" +
                "(CONNECT_DATA=(SERVICE_NAME=service)))";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        KeyValue keyValue = parser.parse();
        Description description = new Description(keyValue);

        Description value = new Description();
        value.setServiceName("service");
        value.addAddress("tcp", "1.2.3.4", "1521");
        value.addAddress("tcp", "1.2.3.5", "1522");
        Assert.assertEquals(description, value);

    }

    @Test
    public void parse2() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCOL = TCP)(HOST = 1.2.3.4 ) (PORT = 1521 ))" +
                "(ADDRESS = (PROTOCOL = TCP ) (HOST = 1.2.3.5) ( PORT = 1522 ))" +
                " ( CONNECT_DATA = ( SERVICE_NAME = service ) ) )";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        KeyValue keyValue = parser.parse();
        Description description = new Description(keyValue);

        Description value = new Description();
        value.setServiceName("service");
        value.addAddress("tcp", "1.2.3.4", "1521");
        value.addAddress("tcp", "1.2.3.5", "1522");
        Assert.assertEquals(description, value);

    }

    @Test
    public void parse3() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCOL = TCP)(HOST = 1.2.3.4 ) (PORT = 1521 ))" +
                "(ADDRESS = (PROTOCOL = TCP ) (HOST = 1.2.3.5) ( PORT = 1522 ))" +
                "(ADDRESS = (PROTOCOL = TCP ) (HOST = 1.2.3.6) ( PORT = 1523 ))" +
                " ( CONNECT_DATA = ( SERVICE_NAME = service ) ) )";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        KeyValue keyValue = parser.parse();
        Description description = new Description(keyValue);

        Description value = new Description();
        value.setServiceName("service");
        value.addAddress("tcp", "1.2.3.4", "1521");
        value.addAddress("tcp", "1.2.3.5", "1522");
        value.addAddress("tcp", "1.2.3.6", "1523");
        Assert.assertEquals(description, value);

    }

    @Test
    public void parse4() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCOL = TCP)(HOST = 1.2.3.4 ) (PORT = 1521 ))" +
                " ( CONNECT_DATA = ( SID = sid ) ) )";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        KeyValue keyValue = parser.parse();
        Description description = new Description(keyValue);

        Description value = new Description();
        value.setSid("sid");
        value.addAddress("tcp", "1.2.3.4", "1521");
        Assert.assertEquals(description, value);

    }

    @Test
     public void parseEofError() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCOL = TCP)(HOST = 1.2.3.4 ) (PORT = 1521 ))" +
//                " ( CONNECT_DATA = ( SID = sid ) ) )";
                " ( CONNECT_DATA = ( SID = sid ) ) ";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        try {
            KeyValue keyValue = parser.parse();
            Assert.fail();
        } catch (OracleConnectionStringException e) {
            logger.info("Expected error", e);
        }


    }


    @Test
    public void parseSyntaxError() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCOL = TCP) HOST = 1.2.3.4 ) (PORT = 1521 ))" +
                " ( CONNECT_DATA = ( SID = sid ) ) )";

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        try {
            KeyValue keyValue = parser.parse();
            Assert.fail();
        } catch (OracleConnectionStringException e) {
            logger.info("Expected error", e);
        }
    }

    @Test
    public void parseSyntaxError2() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCOL = TCP) (HOST = 1.2.3.4 ) ( = 1521 ))" +
                " ( CONNECT_DATA = ( SID = sid ) ) )";
        // removed port

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        try {
            KeyValue keyValue = parser.parse();
            Assert.fail();
        } catch (OracleConnectionStringException e) {
            logger.info("Expected error", e);
        }
    }

}
