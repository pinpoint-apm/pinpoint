package com.nhn.pinpoint.web.cluster;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author koo.taejin <kr14910>
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
