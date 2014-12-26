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

package com.navercorp.pinpoint.web.cluster;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author koo.taejin
 *
 */
public class CollectorClusterInfoRepository {

	private static final Charset charser = Charset.forName("UTF-8");

	private static final String PROFILER_SEPERATOR = "\r\n";

	private final Map<String, Map<String, String>> repository = new HashMap<String, Map<String, String>>();

	private final Object lock = new Object();
	
	public void put(String id, byte[] data) {
		synchronized (lock) {
			Map<String, String> newMap = new HashMap<String, String>();

			String[] profilerInfoList = new String(data, charser).split(PROFILER_SEPERATOR);

			for (String profilerInfo : profilerInfoList) {
				if (profilerInfo == null || profilerInfo.trim().equals("")) {
					continue;
				}

				newMap.put(profilerInfo, id);
			}
			
			repository.put(id, newMap);
		}
	}

	public void remove(String id) {
		synchronized (lock) {
			repository.remove(id);
		}
	}

	public List<String> get(String applicationName, String agentId, long startTimeStamp) {
		List<String> result = new ArrayList<String>();

		synchronized (lock) {
			String key = bindingKey(applicationName, agentId, startTimeStamp);
			for (Map<String, String> eachCollectorClusterInfo : repository.values()) {
				String collectorId = eachCollectorClusterInfo.get(key);
				if (collectorId != null) {
					result.add(collectorId);
				}
			}
		}

		return result;
	}

	public void clear() {
		synchronized (lock) {
			repository.clear();
		}
	}

	private String bindingKey(String applicationName, String agentId, long startTimeStamp) {
		StringBuilder key = new StringBuilder();

		key.append(applicationName);
		key.append(":");
		key.append(agentId);
		key.append(":");
		key.append(startTimeStamp);

		return key.toString();
	}
	
	@Override
	public String toString() {
		return repository.toString();
	}

}
