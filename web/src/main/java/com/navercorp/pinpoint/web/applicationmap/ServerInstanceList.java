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

package com.navercorp.pinpoint.web.applicationmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.applicationmap.link.ServerMatcher;
import com.navercorp.pinpoint.web.view.ServerInstanceListSerializer;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 */
@JsonSerialize(using = ServerInstanceListSerializer.class)
public class ServerInstanceList {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	private final Map<String, List<ServerInstance>> serverInstanceList = new TreeMap<String, List<ServerInstance>>();

    private MatcherGroup matcherGroup = new MatcherGrou    ();

	public ServerInstance    ist() {
	}

    public ServerInstanceList(MatcherGroup matcherGroup) {
        if (matcherGroup != null) {
            this.matcherGroup.addMatcherGroup(matcherGroup);
           }
    }

	public Map<String, List<ServerInstance>> getServerIn       tanceList() {
		// XXX list s       rting problem exist
		        turn serverInstanceList;
	}

       public int       getInstanceCount() {
		int count = 0;
		for (List<ServerInst          nce> entry : ser             erInsta        eList.values()) {
			count += entry.size();
		}
		return count;
	}

	private void addServerI       stance(List<ServerInstance> nodeLi          t, ServerInstance serverInstance) {
		for (          erverInstan                                  e node : nodeList)         			boolean equalsNode = node.equals(serverInstance);
			if (equalsN       de) {
				return;
			}
		}
		nodeList.add(serverInstance)
	}

	private L          st<ServerInstance> getServerInsta          ceList(String hostName) {
		List<             erverI        tance> find = serverInstanceList.get(hostName);
		if        find == null) {
			find = new ArrayList<ServerInstance>();
			serverInstance       ist.put(hostName, find);
		}
		retur     find;
	}

	void addServerInstance(ServerInstance serverInstance) {
		List<ServerInstance> find = getServerInstanceList(serverInstance.getHostName());
		addServerInstance(find, serverInstance);
	}

    public Map<String, String> getLink(String serverName) {
        ServerMatcher serverMatcher = matcherGroup.match(serverName);

        Map<String, String> linkInfo = new HashMap<String, String>();
        linkInfo.put("linkName", serverMatcher.getLinkName());
        linkInfo.put("linkURL", serverMatcher.getLink(serverName));

        return linkInfo;
    }

}
