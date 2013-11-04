package com.nhn.pinpoint.profiler.modifier.db.oracle.parser;

import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.Description;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.KeyValue;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.OracleConnectionStringException;
import com.nhn.pinpoint.profiler.modifier.db.oracle.parser.OracleNetConnectionDescriptorParser;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        // port 제거

        OracleNetConnectionDescriptorParser parser = new OracleNetConnectionDescriptorParser(rac);
        try {
            KeyValue keyValue = parser.parse();
            Assert.fail();
        } catch (OracleConnectionStringException e) {
            logger.info("Expected error", e);
        }
    }

}
