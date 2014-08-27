package com.nhn.pinpoint.web.filter;

import java.util.HashMap;
import java.util.List;

/**
 * filter할 때 필요한 힌트 들.
 * 
 * @author netspider
 * 
 */
// FIXME 이런 deserializer 만드는 방법을 모르겠다 ㅡㅡ;
public class FilterHint extends HashMap<String, List<Object>> {

	private static final long serialVersionUID = -8765645836014210889L;

	public static final String EMPTY_JSON = "{}";

	public boolean containApplicationHint(String applicationName) {
		List<Object> list = get(applicationName);

		if (list == null) {
			return false;
		} else {
			return !list.isEmpty();
		}
	}

	public boolean containApplicationEndpoint(String applicationName, String endPoint, int serviceTypeCode) {
		if (!containApplicationHint(applicationName)) {
			return false;
		}

		List<Object> list = get(applicationName);

		for (int i = 0; i < list.size(); i += 2) {
			if (endPoint.equals(list.get(i))) {
				if (serviceTypeCode == (Integer) list.get(i + 1)) {
					return true;
				}
			}
		}

		return false;
	}
}
