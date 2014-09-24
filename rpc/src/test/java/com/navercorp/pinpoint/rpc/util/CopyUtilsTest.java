package com.nhn.pinpoint.rpc.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class CopyUtilsTest {

	@Test
	public void copyUtilsTest() {
		Map original = createSimpleMap("key", 2);

		Map copied = CopyUtils.mediumCopyMap(original);
		Assert.assertEquals(2, copied.get("key"));
		Assert.assertEquals(2, original.get("key"));

		original.put("key", 4);
		copied.put("key", 3);
		copied.put("new", "new");

		Assert.assertEquals(3, copied.get("key"));
		Assert.assertEquals("new", copied.get("new"));
		Assert.assertEquals(4, original.get("key"));
	}

	@Test
	public void copyUtilsTest2() {
		Map original = createSimpleMap("key", 2);

		Map innerMap = createSimpleMap("innerKey", "inner");
		original.put("map", innerMap);

		Map copied = CopyUtils.mediumCopyMap(original);

		Assert.assertEquals(2, copied.get("key"));
		Assert.assertEquals("inner", ((Map) copied.get("map")).get("innerKey"));
		Assert.assertEquals(2, original.get("key"));
		Assert.assertEquals("inner", ((Map) original.get("map")).get("innerKey"));

		original.put("key", 3);
		copied.put("key", 4);
		
		innerMap.put("innerKey", "key");
		Map copiedInnerMap = (Map) copied.get("map");
		copiedInnerMap.put("test", "test");
		
		Assert.assertEquals(4, copied.get("key"));
		Assert.assertEquals("inner", ((Map) copied.get("map")).get("innerKey"));
		Assert.assertEquals("test", ((Map) copied.get("map")).get("test"));
		Assert.assertEquals(3, original.get("key"));
		Assert.assertEquals("key", ((Map) original.get("map")).get("innerKey"));
		Assert.assertFalse(((Map) original.get("map")).containsKey("test"));
	}

	private Map createSimpleMap(Object key, Object value) {
		Map map = new HashMap();
		map.put(key, value);

		return map;
	}

}
