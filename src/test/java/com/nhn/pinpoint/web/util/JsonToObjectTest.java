package com.nhn.pinpoint.web.util;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

public class JsonToObjectTest {

	@Test
	public void decode() {
		try {

			String s = "%5B%7B%22fa%22%3A%22FRONT-WEB%22%2C%22fst%22%3A%22TOMCAT%22%2C%22ta%22%3A%22BACKEND-API%22%2C%22tst%22%3A%22TOMCAT%22%7D%5D";

			String d = URLDecoder.decode(s, "UTF-8");
			System.out.println(d);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Test
	public void convert() {
		StringBuilder json = new StringBuilder();
		json.append("[{");
		json.append("\"FA\" : \"FROM_APPLICATION\",");
		json.append("\"FAT\" : \"FROM_APPLICATION_TYPE\",");
		json.append("\"TA\" : \"TO_APPLICATION\",");
		json.append("\"TAT\" : \"TO_APPLICATION_TYPE\",");
		json.append("\"RF\" : 0,");
		json.append("\"RT\" : 1000,");
		json.append("\"IE\" : 1,");
		json.append("\"UP\" : \"/**\"");
		json.append("}]");

		try {
			ObjectMapper om = new ObjectMapper();
			List<Map<String, Object>> list = om.readValue(json.toString(), new TypeReference<List<Map<String, Object>>>() {
			});

			Assert.assertEquals(1, list.size());

			Map<String, Object> readValue = list.get(0);

			Assert.assertEquals("FROM_APPLICATION", readValue.get("FA"));
			Assert.assertEquals("FROM_APPLICATION_TYPE", readValue.get("FAT"));
			Assert.assertEquals("TO_APPLICATION", readValue.get("TA"));
			Assert.assertEquals("TO_APPLICATION_TYPE", readValue.get("TAT"));
			Assert.assertEquals(0, readValue.get("RF"));
			Assert.assertEquals(1000, readValue.get("RT"));
			Assert.assertEquals(1, readValue.get("IE"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void invalidJson() {
		String invalidJsonStr = "INVALID";

		try {
			ObjectMapper om = new ObjectMapper();
			Object readValue = om.readValue(invalidJsonStr, new TypeReference<Map<String, Object>>() {
			});
			Assert.fail();
		} catch (Exception e) {
		}
	}
}
