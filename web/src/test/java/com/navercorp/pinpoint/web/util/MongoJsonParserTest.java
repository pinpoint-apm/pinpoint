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

package com.navercorp.pinpoint.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Roy Kim
 */
public class MongoJsonParserTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MongoJsonParser jsonParser = new DefaultMongoJsonParser();
    private OutputParameterMongoJsonParser outputParameterMongoJsonParser = new OutputParameterMongoJsonParser();


    @Test
    public void combineBindValue() {

        String test = "{\"items\" : [ { \"title\" : \"First Item\",  \"description\" : \"This is the first item description.\"  }, { \"title\" : \"Second Item\",  \"description\" : \"This is the second item description.\" }]}";
        String testexpect = "{\"items\" : [ { \"title\" : \"?\",  \"description\" : \"?\"  }, { \"title\" : \"?\",  \"description\" : \"?\" }]}";
        String testparam = "\"First Item\",\"This is the first item description.\",\"Second Item\",\"This is the second item description.\"";
        assertCombine(test, testexpect, testparam);

        assertCombine("{ \"_id\" : { \"$oid\" : \"5b4efec5097684a1d1d5c658\" }, \"name\" : \"bsonDocument\", \"company\" : \"Naver\" }"
                , "{ \"_id\" : { \"$oid\" : \"?\" }, \"name\" : \"?\", \"company\" : \"?\" }"
                , "\"5b4efec5097684a1d1d5c658\",\"bsonDocument\",\"Naver\"");

        assertCombine("{\"collection\": {\"id\": [tag1,pizza,coffee,snacks,tag2],\"passwordProtected\": false,\"typeLabel\": \"blog\"}}"
                , "{\"collection\": {\"id\": \"?\",\"passwordProtected\": \"?\",\"typeLabel\": \"?\"}}"
                , "[tag1,pizza,coffee,snacks,tag2],false,\"blog\"");

        assertCombine("{\"collection\": class = {tag1,pizza,coffee,snacks,tag2}}"
                , "{\"collection\": \"?\"}"
                , "class = {tag1,pizza,coffee,snacks,tag2}");

    }

    private void assertCombine(String result, String json, String outputParams) {

        List<String> bindValues = outputParameterMongoJsonParser.parseOutputParameter(outputParams);
        String full = jsonParser.combineBindValues(json, bindValues);

        Assert.assertEquals(result, full);
    }
}
