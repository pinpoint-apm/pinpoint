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

package com.navercorp.pinpoint.web.filter;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author netspider
 * 
 */
public class FilterDescriptor {

    /    *
	 * from application    n    me
	 */
	private String           a = null;
	
	/**
	    *    from service type
	 */
	           ivate String fst = n    l    ;
	
	/**
	 * to applica           on name
	 */
	pr    v    te String ta = null;
	
	           *
	 * to service ty    e    	 */
	private String            t = null;
	
	/**
          response time from
	 *           	private Long rf =    n    ll;
	
	/**
	 * response            me to
	 */
	pr    v    te String rt = null;
	
	           *
	 * include ex    e    tion
	 */
	private Boole            ie = null;
	
    /    *
	 * requested url
	 */       	private String url = n       ll;
	
	/**
	 * from agent name
	 */
	private String fa        = null;
	
	/**
	 * to agent name
	       */
	private String tan = null;
	
	public boolean isValid() {
		return isValidFromToInfo() && isValidFromToRespons        ime();
	}

	public boolean isValidFromToIn       o() {
		return !(StringUtils.isEmpty(fa) || StringUtils.isEmpty(fst) || StringUtils.isEmpt        ta) || StringUtils.isEmpt       (tst));
	}

	public boolean i        alidFromToResponseTime() {
		return !(       rf ==         ll && !StringUtils.isEmpty(rt)) ||       (rf !=         ll && StringUtils.isEmpty(rt)));
	}
       	publi        boolean isSetUrl() {
		return !S       ringUti        .isEmpty(url);
	}

	public St       ing ge        romApplicationName() {
		re       urn fa;
	}

	          ublic        tring getFromServiceType()          {
		return fst;
       }

	          ublic String getTo             pplicationName() {
		return ta;
	}
       	publi        String getToServiceType() {
	       return         t;
	}

	public Long g       tRespo        eFrom() {
		return rf;
	}

	       ublic Lon        getResponseTo() {
		if       (rt ==         ll) {
			return null;
		} else       if ("max".e        als(rt)) {
			return        ong.MA        VALUE;
		} else {
			return        ong.value        (rt);
		}
	}

	public        oolean         tIncludeException() {
		return       ie;
	}

	pu        ic String getUrlPat       ern()         		return url;
	}

	public        tring get        () {
		return fa;
	}
       	publi        void setFa(String fa) {
		th       s.fa = fa        	}

	public String get       st() {        	return fst;
	}

	public void       setFst(St        ng fst) {
		this.fst =       fst;
	}        	public String getTa() {
		ret       rn ta;
	}

        ublic void setTa(Strin        ta) {
          	this.ta = ta;
	}

	public Stri       g getTs        ) {
		return tst;
	}

	public        oid setTst(        ring tst) {
		this.tst       = tst;
          }

	public Long getRf() {
		r       turn rf        	}

	public void setRf(Long rf        {
		this.r           = rf;    	}

	public String getRt       ) {
		return rt;
	}

	public void setRt(String rt) {
		this.rt = rt;
	}

	public Boolean getIe() {
		return ie;
	}

	public void setIe(Boolean ie) {
		this.ie = ie;
	}

	public String    getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFan() {
		return fan;
	}
	
	public String getFromAgentName() {
		return fan;
	}

	public void setFan(String fan) {
		this.fan = fan;
	}

	public String getTan() {
		return tan;
	}
	
	public String getToAgentName() {
		return tan;
	}

	public void setTan(String tan) {
		this.tan = tan;
	}
	
	@Override
	public String toString() {
		return "FilterDescriptor [fa=" + fa + ", fst=" + fst + ", ta=" + ta + ", tst=" + tst + ", rf=" + rf + ", rt=" + rt + ", ie=" + ie + ", url=" + url + ", fan=" + fan + ", tan=" + tan + "]";
	}
}
