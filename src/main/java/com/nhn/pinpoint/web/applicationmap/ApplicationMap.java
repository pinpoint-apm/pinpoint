package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nhn.pinpoint.web.vo.*;
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
        this.linkList = linkList;
	}

    @JsonProperty("nodeDataArray")
    public Collection<Node> getNodes() {
		return this.nodeList.getNodeList();
	}

    @JsonProperty("linkDataArray")
	public Collection<Link> getLinks() {
		return this.linkList.getLinkList();
	}


    public void setApplicationScatterScanResult(List<ApplicationScatterScanResult> applicationScatterScanResultList) {
        this.applicationScatterScanResultList = applicationScatterScanResultList;
    }

    @JsonIgnore
    public List<ApplicationScatterScanResult> getApplicationScatterScanResultList() {
        return applicationScatterScanResultList;
    }





}
