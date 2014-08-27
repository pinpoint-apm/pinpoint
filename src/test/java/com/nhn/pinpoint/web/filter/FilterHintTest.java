package com.nhn.pinpoint.web.filter;

import junit.framework.Assert;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author netspider
 * 
 */
public class FilterHintTest {
	private final ObjectMapper om = new ObjectMapper();

	@Test
	public void convert() {
		StringBuilder json = new StringBuilder();
		json.append("{ \"TO_APPLICATION\" : [\"IP1\", 1,\"IP2\", 2], \"TO_APPLICATION2\" : [\"IP3\", 3,\"IP4\", 4] }");

		try {
			FilterHint hint = om.readValue(json.toString(), new TypeReference<FilterHint>() {
			});

			Assert.assertNotNull(hint);
			Assert.assertEquals(2, hint.size());

			Assert.assertTrue(hint.containApplicationHint("TO_APPLICATION"));
			Assert.assertTrue(hint.containApplicationHint("TO_APPLICATION2"));
			Assert.assertFalse(hint.containApplicationHint("TO_APPLICATION3"));

			Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP1", 1));
			Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION", "IP2", 2));

			Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP3", 3));
			Assert.assertTrue(hint.containApplicationEndpoint("TO_APPLICATION2", "IP4", 4));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void empty() {
		StringBuilder json = new StringBuilder();
		json.append("{}");
		
		try {
			FilterHint hint = om.readValue(json.toString(), new TypeReference<FilterHint>() {
			});
			
			Assert.assertNotNull(hint);
			Assert.assertTrue(hint.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
