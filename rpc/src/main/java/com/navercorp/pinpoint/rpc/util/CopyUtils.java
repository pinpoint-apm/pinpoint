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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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
	 *
	 * DeepCopy for basic type like Map, List.
	 * ShallCopy for Bean.
	 *
	 * Copy 
	 *
	 */
	public static Map<Object, Object> mediumCopyMap(Map<Object, Object> original) {
		Map<Object, Object> result = new LinkedHashMap<Object, Object>();

        for (Map.Entry<Object, Object> entry : original.entrySet()) {
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
	public static Collection<Object> mediumCopyCollection(Collection<Object> original) {
		return new ArrayList<Object>(original);
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
