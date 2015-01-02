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

package com.navercorp.pinpoint.rpc.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.rpc.util.CopyUtils;

public class CopyUtilsTest {

    @Te    t
	public void copyUtilsTes       () {
		Map original = createSimpleMap(       key", 2);

		Map copied = CopyUtils.mediumC       pyMap(original);
		Assert.assertEquals       2, copied.get("key"));
		Assert.assertEqu       ls(2, original.get(       key"));

		origin       l.put("key", 4);
		cop       ed.put("key", 3);
		copied.put("new",        new");

		Assert.assertEquals(3, copied.ge       ("key"));
		Assert.assertEquals("new", c        ied    get("new"));
		Assert.assert       quals(4, original.get("key"));
	}

	@T       st
	public void copyUtilsTest2() {
		Map origina        = createSimpleMap("key", 2       ;

		Map innerMap = createSimpleMap("innerKe       ", "inner");
		original.put("map", inn       rMap);

		Map copied = CopyUtils.mediumCopyMap(original);

		Assert.       ssertEquals(2, copied.get("key"));
		Ass       rt.assertEquals("inner", ((Map) copied.get("map")).get("innerKey"));
		       ssert.assertEquals(       , original.get("k             y"));
		Assert.assertEqua       s("inner", ((Map) original.get("map")).ge       ("innerKey"));

		original.put(             key", 3);
		copied.put("key", 4);

		innerMap.put("innerKey", "key");
		Map copiedInnerMap = (Map) cop       ed.get("map");
		copiedInnerMap.put("test", "test");
		
		Asser       .assertEquals(4, copied.get("key"));
		A       sert.assertEquals("inner", ((Map) copied.get("map")).get("innerKey")       ;
		Assert.assertEquals("test", ((Map) copied.get("map")).get("t        t"));
		Assert.assertEquals(3, original.get("key"));
       	Assert.assertEquals       "key", ((Map) ori       inal.ge    ("map")).get("innerKey"));
		Assert.assertFalse(((Map) original.get("map")).containsKey("test"));
	}

	private Map createSimpleMap(Object key, Object value) {
		Map map = new HashMap();
		map.put(key, value);

		return map;
	}

}
