/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Roy Kim
 */
public class MongoJsonCombineTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MongoJsonParser jsonParser = new DefaultMongoJsonParser();
    private OutputParameterMongoJsonParser outputParameterMongoJsonParser = new OutputParameterMongoJsonParser();


    @Test
    public void combineBindValue() {
        String test = "{\"items\" : [ { \"title\" : \"First Item\",  \"description\" : \"This is the first item description.\"  }, { \"title\" : \"Second Item\",  \"description\" : \"This is the second item description.\" }]}";
        String testexpect = "{\"items\" : [ { \"title\" : \"?\",  \"description\" : \"?\"  }, { \"title\" : \"?\",  \"description\" : \"?\" }]}";
        String testparam = "\"First Item\",\"This is the first item description.\",\"Second Item\",\"This is the second item description.\"";
        assertCombine(test, testexpect, testparam);
    }

    @Test
    public void combineBindValueArray() {
        assertCombine("{ \"Java_Collection\" : [\"naver\", \"apple\", { \"number\" : 3 }], \"_id\" : 5bea4fb796f948fca7e11975 }"
                , "{ \"Java_Collection\" : [\"?\", \"?\", { \"number\" : \"?\" }], \"_id\" : \"?\" }"
                , "\"naver\",\"apple\",3,5bea4fb796f948fca7e11975");
    }

    @Test
    public void combineBindValueNestedArray() {
        assertCombine("{\"collection\": {\"id\": [tag1,pizza,[snacks,tag2],coffee],\"passwordProtected\": false,\"typeLabel\": \"blog\"}}"
                , "{\"collection\": {\"id\": [\"?\",\"?\",[\"?\",\"?\"],\"?\"],\"passwordProtected\": \"?\",\"typeLabel\": \"?\"}}"
                , "tag1,pizza,snacks,tag2,coffee,false,\"blog\"");

        assertCombine("{\"collection\": {\"id\": [tag1,pizza,coffee,[snacks,tag2]],\"passwordProtected\": false,\"typeLabel\": \"blog\"}}"
                , "{\"collection\": {\"id\": [\"?\",\"?\",\"?\",[\"?\",\"?\"]],\"passwordProtected\": \"?\",\"typeLabel\": \"?\"}}"
                , "tag1,pizza,coffee,snacks,tag2,false,\"blog\"");
    }

    @Test
    public void combineBindValueDoubleQuoteInKey() {
        assertCombine("{\"coll\\\"ection\": {\"name\" : \"tag1\", \"na\\\"me2\" : 1}}"
                , "{\"coll\\\"ection\": {\"name\" : \"?\", \"na\\\"me2\" : \"?\"}}"
                , "\"tag1\",1");
    }

    @Test
    public void combineBindValueDoubleQuoteInValue() {
        assertCombine("{\"collection\": {\"name\" : \"ta\"g,1\", \"name2\" : \"\"\"\", \"name3\" : \"\", \"name4\" : }}"
                , "{\"collection\": {\"name\" : \"?\", \"name2\" : \"?\", \"name3\" : \"?\", \"name4\" : \"?\"}}"
                , "\"ta\"\"g,1\",\"\"\"\"\"\",\"\",");
    }

    @Test
    public void combineBindValueEveryElementOfMongoDB() {
        assertCombine("\"query\" : { \"int32\" : { \"value\" : 12 }, \"int64\" : { \"value\" : 77 }, \"boolean\" : { \"value\" : true }, \"date\" : { \"value\" : 1542019151267 }, \"double\" : { \"value\" : 12.3 }, \"string\" : { \"value\" : \"pinpoint\" }, \"objectId\" : { \"value\" : 5be9584f96f948776d3130ec }, \"code\" : { \"code\" : \"int i = 10;\" }, \"codeWithScope\" : { \"code\" : \"int x = y\", \"scope\" : { \"y\" : { \"value\" : 1 } } }, \"regex\" : { \"pattern\" : \"^test.*regex.*xyz$\", \"options\" : \"bgi\" }, \"symbol\" : { \"symbol\" : \"wow\" }, \"timestamp\" : { \"value\" : 1311768464867721221 }, \"undefined\" : { }, \"binary1\" : { \"type\" : 0, \"data\" : [-32, 79, -48, 32, -22, 58, 105, 16, -94, -40, 8, 0, 43, 48, 48, -99] }, \"oldBinary\" : { \"type\" : 2, \"data\" : [1, 1, 1, 1, 1] }, \"arrayInt\" : [{ \"value\" : \"stest\" }, { \"value\" : 111.0 }, { \"value\" : true }, { \"value\" : 7 }], \"document\" : { \"a\" : { \"value\" : 77 } }, \"dbPointer\" : { \"namespace\" : \"db.coll\", \"id\" : 5be9584f96f948776d3130ed }, \"null\" : { } }",
                "\"query\" : { \"int32\" : { \"value\" : \"?\" }, \"int64\" : { \"value\" : \"?\" }, \"boolean\" : { \"value\" : \"?\" }, \"date\" : { \"value\" : \"?\" }, \"double\" : { \"value\" : \"?\" }, \"string\" : { \"value\" : \"?\" }, \"objectId\" : { \"value\" : \"?\" }, \"code\" : { \"code\" : \"?\" }, \"codeWithScope\" : { \"code\" : \"?\", \"scope\" : { \"y\" : { \"value\" : \"?\" } } }, \"regex\" : { \"pattern\" : \"?\", \"options\" : \"?\" }, \"symbol\" : { \"symbol\" : \"?\" }, \"timestamp\" : { \"value\" : \"?\" }, \"undefined\" : { }, \"binary1\" : { \"type\" : \"?\", \"data\" : [\"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\", \"?\"] }, \"oldBinary\" : { \"type\" : \"?\", \"data\" : [\"?\", \"?\", \"?\", \"?\", \"?\"] }, \"arrayInt\" : [{ \"value\" : \"?\" }, { \"value\" : \"?\" }, { \"value\" : \"?\" }, { \"value\" : \"?\" }], \"document\" : { \"a\" : { \"value\" : \"?\" } }, \"dbPointer\" : { \"namespace\" : \"?\", \"id\" : \"?\" }, \"null\" : { } }"
                , "12,77,true,1542019151267,12.3,\"pinpoint\",5be9584f96f948776d3130ec,\"int i = 10;\",\"int x = y\",1,\"^test.*regex.*xyz$\",\"bgi\",\"wow\",1311768464867721221,0,-32,79,-48,32,-22,58,105,16,-94,-40,8,0,43,48,48,-99,2,1,1,1,1,1,\"stest\",111.0,true,7,77,\"db.coll\",5be9584f96f948776d3130ed");
    }

    @Test
    public void combineBindAbbreviationTest() {
        assertCombine("{\"bsons\" : [{ \"name\" : \"manymanay\", \"company\" : \"ManyCompany\" }, ...(99)]}"
                , "{\"bsons\" : [{ \"name\" : \"?\", \"company\" : \"?\" }, \"?\"]}"
                , "\"manymanay\",\"ManyCompany\",...(99)");
    }

    private void assertCombine(String result, String json, String outputParams) {

        List<String> bindValues = outputParameterMongoJsonParser.parseOutputParameter(outputParams);
        logger.debug("start====== ");
        logger.debug("json={} " + json + ", size : " + json.length());
        logger.debug("bindValues={} " + bindValues.get(0) + ", size : " + bindValues.size());
        String full = jsonParser.combineBindValues(json, bindValues);
        logger.debug("full={} " + full + ", size : " + full.length());
        Assert.assertEquals(result, full);
    }
}
