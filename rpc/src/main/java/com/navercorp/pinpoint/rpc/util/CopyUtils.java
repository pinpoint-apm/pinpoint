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
 * used DeepCopy for basic type. ex) like Map, List.
 * used ShallCopy for Bean.
 *
 * There are functional limitations because this class made only for copying securely something like Map.
 * Be careful.
 *
 * @author koo.taejin
 */

public final class CopyUtils {

    private CopyUtils()
	}

	public static Map<Object, Object> mediumCopyMap(Map<Object, Object> ori       inal) {
		Map<Object, Object> result = new LinkedHashMap<Object, Object>();

        for (Map.Entry<Object, Object> entry : original.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            result.put(mediumCopy(key), mediumCopy(valu       ));
              }
		return result;
	}

	public static Collection<Object> mediumCopyCollection(Coll       ction<Object> original) {
		return         w ArrayList<Object>(original);
	}

	private stati        Object mediumCopy(Object           riginal) {
		if (original insta       ceof Map) {
			return mediumCopyMap((Map           original);
		} else if (original instanceof        olle          tion) {
		          return mediumCopyCollection((Collection) original);
		} else {
			return original;
		}
	}

}
