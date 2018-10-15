/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    //@Test
    public void indentCheck() {

        String test = "{\"collection\": {\"id\": [\"tag1\",\"pizza\",\"coffee\",\"snacks\",\"tag2\"],\"passwordProtected\": false,\"typeLabel\": \"blog\"}}";
        String result = "{\n" +
                "  \"collection\" : {\n" +
                "    \"id\" : [ \"tag1\", \"pizza\", \"coffee\", \"snacks\", \"tag2\" ],\n" +
                "    \"passwordProtected\" : false,\n" +
                "    \"typeLabel\" : \"blog\"\n" +
                "  }\n" +
                "}";

        String test2 = "{\"items\" : [\n { \"title\" : \"First Item\", \n \"description\" : \"This is the first, item description.\" \n }, { \"title\" : \"Second Item\", \n \"description\" : \"This is the second item description.\" }]}";
        String result2 = "{\n" +
                "  \"items\" : [ {\n" +
                "    \"title\" : \"First Item\",\n" +
                "    \"description\" : \"This is the first, item description.\"\n" +
                "  }, {\n" +
                "    \"title\" : \"Second Item\",\n" +
                "    \"description\" : \"This is the second item description.\"\n" +
                "  } ]\n" +
                "}";
        String test3 = "{\"items\" : [ { \"title\" : \"First Item\",  \"description\" : \"This is the first, item description.\"  }, { \"title\" : \"Second Item\",  \"description\" : \"This is the second item description.\" }]}";
        String result3 = "{\n" +
                "  \"items\" : [ {\n" +
                "    \"title\" : \"First Item\",\n" +
                "    \"description\" : \"This is the first, item description.\"\n" +
                "  }, {\n" +
                "    \"title\" : \"Second Item\",\n" +
                "    \"description\" : \"This is the second item description.\"\n" +
                "  } ]\n" +
                "}";
        String test4 = "{\"collection\": {\"id\": \"5048fde7c4aa917cbd4d8e13\",\"websiteId\": \"50295e80e4b096e761d7e4d3\",\"enabled\": true,\"starred\": false,\"type\": 1,\"ordering\": 2,\"title\": \"Blog\",\"navigationTitle\": \"Blog\",\"urlId\": \"blog\",\"itemCount\": 2,\"updatedOn\": 1454432700858,\"pageSize\": 20,\"folder\": false,\"dropdown\": false,\"tags\": [\"tag1\",\"pizza\",\"coffee\",\"snacks\",\"tag2\"],\"categories\": [\"category1\",\"category2\"],\"homepage\": false,\"typeName\": \"blog\",\"synchronizing\": false,\"fullUrl\": \"/blog/\",\"passwordProtected\": false,\"typeLabel\": \"blog\"}}";
        String result4 = "{\n" +
                "  \"collection\" : {\n" +
                "    \"id\" : \"5048fde7c4aa917cbd4d8e13\",\n" +
                "    \"websiteId\" : \"50295e80e4b096e761d7e4d3\",\n" +
                "    \"enabled\" : true,\n" +
                "    \"starred\" : false,\n" +
                "    \"type\" : 1,\n" +
                "    \"ordering\" : 2,\n" +
                "    \"title\" : \"Blog\",\n" +
                "    \"navigationTitle\" : \"Blog\",\n" +
                "    \"urlId\" : \"blog\",\n" +
                "    \"itemCount\" : 2,\n" +
                "    \"updatedOn\" : 1454432700858,\n" +
                "    \"pageSize\" : 20,\n" +
                "    \"folder\" : false,\n" +
                "    \"dropdown\" : false,\n" +
                "    \"tags\" : [ \"tag1\", \"pizza\", \"coffee\", \"snacks\", \"tag2\" ],\n" +
                "    \"categories\" : [ \"category1\", \"category2\" ],\n" +
                "    \"homepage\" : false,\n" +
                "    \"typeName\" : \"blog\",\n" +
                "    \"synchronizing\" : false,\n" +
                "    \"fullUrl\" : \"/blog/\",\n" +
                "    \"passwordProtected\" : false,\n" +
                "    \"typeLabel\" : \"blog\"\n" +
                "  }\n" +
                "}";

        assertIndented(test, result);
        assertIndented(test2, result2);
        assertIndented(test3, result3);
        assertIndented(test4, result4);
    }

    private void assertIndented(String actual, String expected) {
        ObjectMapper mapper = new ObjectMapper();
        Object json;
        String indented;

        //actual.replace("\n", "");

        try {
            json = mapper.readValue(actual, Object.class);
            indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            //logger.warn("Indent Success = \n{} ", indented);
        } catch (Exception e) {
            logger.warn("Indent failed Exception: ", e);
            indented = null;
        }

        try {
            Assert.assertEquals("normalizeJson check", expected, indented);
        } catch (AssertionError e) {
            logger.warn("Original :\n{}\n{}", expected, indented);
            throw e;
        }
    }


    @Test
    public void combineBindValue() {
        OutputParameterMongoJsonParser parameterParser = new OutputParameterMongoJsonParser();

        String test = "{\"items\" : [\n { \"title\" : \"First Item\", \n \"description\" : \"This is the first item description.\" \n }, { \"title\" : \"Second Item\", \n \"description\" : \"This is the second item description.\" }]}";
        String testexpect = "{\"items\" : [\n { \"title\" : \"?\", \n \"description\" : \"?\" \n }, { \"title\" : \"?\", \n \"description\" : \"?\" }]}";
        String testparam = "\"First Item\",\"This is the first item description.\",\"Second Item\",\"This is the second item description.\"";
        assertCombine(test, testexpect, testparam);

        assertCombine("{ \"_id\" : { \"$oid\" : \"5b4efec5097684a1d1d5c658\" }, \"name\" : \"bsonDocument\", \"company\" : \"Naver\" }",
                "{ \"_id\" : { \"$oid\" : \"?\" }, \"name\" : \"?\", \"company\" : \"?\" }", "\"5b4efec5097684a1d1d5c658\",\"bsonDocument\",\"Naver\"");

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
