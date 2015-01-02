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

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.navercorp.pinpoint.web.vo.*;
import com.navercorp.pinpoint.web.vo.scatter.ApplicationScatterScanResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node map
 * 
 * @author netspider
 * @author emeroad
 */
public class ApplicationMap {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NodeList nodeList;
    private final LinkList linkList;

    private final Range range;

    private List<ApplicationScatterScanResult> applicationScatterScanResultList;

	ApplicationMap(Range range, NodeList nodeList, LinkList linkList) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (nodeList == null) {
            throw new NullPointerException("nodeList must not be null");
        }
        if (linkList == null) {
            throw new NullPointerException("linkList must not be null");
        }
        this.range = range;
        this.nodeList = nodeList;
        this.linkList = link    ist;
	}

    @JsonProperty("nodeDataArray")
    public Collection<Node> getN       des() {
		return this.nodeList.    etNodeList();
	}

    @JsonProperty    "linkDataArray")
	public Collectio       <Link> getLinks() {
		return th    s.linkList.getLinkList();
	}


    public void setApplicationScatterScanResult(List<ApplicationScatterScanResult> applicationScatterScanResultList) {
        this.applicationScatterScanResultList = applicationScatterScanResultList;
    }

    @JsonIgnore
    public List<ApplicationScatterScanResult> getApplicationScatterScanResultList() {
        return applicationScatterScanResultList;
    }





}
