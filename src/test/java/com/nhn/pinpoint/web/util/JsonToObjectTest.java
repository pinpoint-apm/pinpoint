package com.nhn.pinpoint.web.util;

import java.util.Map;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

public class JsonToObjectTest {

	@Test
	public void convert() {
		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"FA\" : \"FROM_APPLICATION\",");
		json.append("\"FAT\" : \"FROM_APPLICATION_TYPE\",");
		json.append("\"TA\" : \"TO_APPLICATION\",");
		json.append("\"TAT\" : \"TO_APPLICATION_TYPE\",");
		json.append("\"RF\" : 0,");
		json.append("\"RT\" : 1000,");
		json.append("\"IE\" : 1,");
		json.append("\"UP\" : \"/**\"");
		json.append("}");

		try {
			ObjectMapper om = new ObjectMapper();
			Map<String, Object> readValue = om.readValue(json.toString(), new TypeReference<Map<String, Object>>() {
			});
			
			Assert.assertEquals("FROM_APPLICATION", readValue.get("FA"));
			Assert.assertEquals("FROM_APPLICATION_TYPE", readValue.get("FAT"));
			Assert.assertEquals("TO_APPLICATION", readValue.get("TA"));
			Assert.assertEquals("TO_APPLICATION_TYPE", readValue.get("TAT"));
			Assert.assertEquals(0, readValue.get("RF"));
			Assert.assertEquals(1000, readValue.get("RT"));
			Assert.assertEquals(1, readValue.get("IE"));
			
			Long rt = readValue.containsKey("RT") ? Long.valueOf(readValue.get("RT").toString()) : null;
			
			System.out.println(rt);
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
