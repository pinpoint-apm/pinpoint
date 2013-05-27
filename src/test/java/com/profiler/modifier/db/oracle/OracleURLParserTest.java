package com.profiler.modifier.db.oracle;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 *
 */
public class OracleURLParserTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void tokenWord() {
//        assertTrueTokenTest(OracleURLParser.TOKEN_WORD, "test");
//        assertTrueTokenTest(OracleURLParser.TOKEN_WORD, "111");
//        assertTrueTokenTest(OracleURLParser.TOKEN_WORD, "111");
//        assertTrueTokenTest(OracleURLParser.TOKEN_WORD, "1zc1.1w.sdfsd");
//        assertTrueTokenTest(OracleURLParser.TOKEN_WORD, "127.0.0.1");


    }

    @Test
    public void toekWorkfalse() {
//        assertFalseTokenTest(OracleURLParser.TOKEN_WORD, " 111");
//        assertFalseTokenTest(OracleURLParser.TOKEN_WORD, "111 ");
//        assertFalseTokenTest(OracleURLParser.TOKEN_WORD, "!@#");
    }

//    private void assertTrueTokenTest(String token, String str) {
//        Pattern compile = Pattern.compile(token);
//        Matcher test = compile.matcher(str);
//        Assert.assertTrue("token:" + token + " str:" + str, test.matches());
//    }
//
//    private void assertFalseTokenTest(String token, String str) {
//        Pattern compile = Pattern.compile(token);
//        Matcher test = compile.matcher(str);
//        Assert.assertFalse("token:" + token + " str:" + str, test.matches());
//    }
//
//    public void testHOSTKeyValue() {
//        assertTrueTokenTest(OracleURLParser.TOKEN_HOST_KEY_VALUE, "HOST=123");
//        assertTrueTokenTest(OracleURLParser.TOKEN_HOST_KEY_VALUE, "HOST = 123 ");
//        assertTrueTokenTest(OracleURLParser.TOKEN_HOST_KEY_VALUE, "  HOST =123111 ");
//    }


    @Test
    public void parse() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1522))" +
                "(CONNECT_DATA=(SERVICE_NAME=service)))";

        OracleURLParser parser = new OracleURLParser(rac);
        KeyValue keyValue = parser.parse();

        assertParsingResult(keyValue, 4);

    }

    @Test
    public void parse2() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCO L = TCP)(HOST = 1.2.3.4 ) (PORT = 1521 ))" +
                "(ADDRESS = (PROTOCOL = TCP ) (HOST = 1.2.3.5) ( PORT = 1522 ))" +
                " ( CONNECT_DATA = ( SERVICE_NAME = service ) ) )";

        OracleURLParser parser = new OracleURLParser(rac);
        KeyValue keyValue = parser.parse();

        assertParsingResult(keyValue, 4);

    }

    @Test
    public void parse3() {
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE = off )" +
                "(ADDRESS  =  ( PROTOCO L = TCP)(HOST = 1.2.3.4 ) (PORT = 1521 ))" +
                "(ADDRESS = (PROTOCOL = TCP ) (HOST = 1.2.3.5) ( PORT = 1522 ))" +
                "(ADDRESS = (PROTOCOL = TCP ) (HOST = 1.2.3.6) ( PORT = 1523 ))" +
                " ( CONNECT_DATA = ( SERVICE_NAME = service ) ) )";

        OracleURLParser parser = new OracleURLParser(rac);
        KeyValue keyValue = parser.parse();

        assertParsingResult(keyValue, 5);

    }


    private void assertParsingResult(KeyValue keyValue, int nodeSize) {
        logger.info(keyValue.toString());

        List<KeyValue> keyValueList = keyValue.getKeyValueList();
        Assert.assertEquals(keyValueList.size(), nodeSize);
        Assert.assertEquals(keyValueList.get(0).getKey(), "load_balance");
        Assert.assertEquals(keyValueList.get(1).getKey(), "address");
        Assert.assertEquals(keyValueList.get(2).getKey(), "address");

        Assert.assertEquals(keyValueList.get(nodeSize-1).getKey(), "connect_data");

        Assert.assertEquals(keyValueList.get(1).getKeyValueList().size(), 3);

        Assert.assertEquals(keyValueList.get(1).getKeyValueList().get(0).getValue(), "tcp");
        Assert.assertEquals(keyValueList.get(1).getKeyValueList().get(1).getValue(), "1.2.3.4");
        Assert.assertEquals(keyValueList.get(1).getKeyValueList().get(2).getValue(), "1521");

        Assert.assertEquals(keyValueList.get(2).getKeyValueList().get(0).getValue(), "tcp");
        Assert.assertEquals(keyValueList.get(2).getKeyValueList().get(1).getValue(), "1.2.3.5");
        Assert.assertEquals(keyValueList.get(2).getKeyValueList().get(2).getValue(), "1522");

        boolean findService = false;
        for (KeyValue kv :  keyValue.getKeyValueList()) {
            if (kv.getKey().equals("connect_data")) {
                kv.getKeyValueList().get(0).equals("service");
                findService = true;
            }
        }
        if (!findService) {
            Assert.fail("service value not found");
        }

    }

}
