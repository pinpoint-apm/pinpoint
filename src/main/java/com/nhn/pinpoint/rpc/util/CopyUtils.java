package com.nhn.pinpoint.rpc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author koo.taejin
 */
public final class CopyUtils {

	private CopyUtils() {
	}

	/**
	 * Map, List 등의 기본 타입들은 DeepCopy를 하고, Bean등은 ShallowCopy를 함 
	 * Pinpoint의 Map등을 안전하게 복사할때만 사용하려고 하기 떄문에 기능에 제약이 있음 
	 * 사용하려면 기능제약을 확실히 알고 사용하기를 권함
	 */
	public static Map mediumCopyMap(Map original) {
		Map result = new LinkedHashMap();

		Iterator<Map.Entry> iterator = original.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = iterator.next();
			Object key = entry.getKey();
			Object value = entry.getValue();

			result.put(mediumCopy(key), mediumCopy(value));
		}
		return result;
	}

	/**
	 * Map, List 등의 기본 타입들은 DeepCopy를 하고, Bean등은 ShallowCopy를 함 
	 * Pinpoint의 Map등을 안전하게 복사할때만 사용하려고 하기 떄문에 기능에 제약이 있음 
	 * 사용하려면 기능제약을 확실히 알고 사용하기를 권함
	 */
	public static Collection mediumCopyCollection(Collection original) {
		List result = new ArrayList();

		for (Object each : original) {
			result.add(mediumCopy(each));
		}

		return result;
	}

	private static Object mediumCopy(Object original) {
		if (original instanceof Map) {
			return mediumCopyMap((Map) original);
		} else if (original instanceof Collection) {
			return mediumCopyCollection((Collection) original);
		} else {
			return original;
		}
	}

}
