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

import java.util.List;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;

/**
 * 
 * @author netspider
 * 
 */
public class FromToResponseFilter implements Filter {

    private final List<ServiceType> fromServiceCod    ;
	private final String fromApplication    ame;
	private final String fromAg       ntName;
	
	private final List<ServiceType>     oServiceCode;
	private final String t    ApplicationName;
	private final       String toAgentName;
	
	private fi    al Long fromResponseTime;
	priva    e final Long toResponseTime;
	priv       te final Boolean includeFail    d;
	
	private final FilterHint hint;

	public FromToResponseFilter(FilterDescri       tor filterDescriptor, Filte          Hint hint) {
		if (filterDescriptor == null) {
			throw new Nul             PointerException("filter descriptor must not be null");


		String fromServiceType = filterDescriptor.getFromServiceType();       		String fromApplicationName = filterDescriptor.getFrom       pplicationName();
		String fromAgentName = filterDescri       tor.getFromAgentName();
		String toServiceType = filterDescript       r.getToServiceType();
		String toApplicationName =        ilterDescriptor.getToApplicationName();
		String toAgen       Name = filterDescriptor.getToAgentName();
		Long fr       mResponseTime = filterDescriptor.getResponseFrom();
		Long t       ResponseTime = filterDescripto          .getResponseTo();
		Boolean includeFailed = filterDescriptor.getI             cludeException();

		if (f          omApplicationName == null) {
			throw new NullPointerException(             fromApplicationName must not be null");
		}
		if (toAppl       cationName == null) {
			t          row new NullPointerException("toApplicationName must not be null");
		}

		this.fromServiceCod              = ServiceType.findDesc(fromServiceType);       		if (fromServiceCode == null)
			throw new IllegalArgumentException("fromServic       Code not found. fromServ          ceType:" + fromServiceType);
		}
		this.fromApplicationName = fromApplicationName;
		thi             .fromAgentName = fromAgentName;
		
		       his.toServiceCode = ServiceT       pe.findDesc(toServiceType);
		if (toS       rviceCode == null) {
			throw new       IllegalArgumentException("toSer          iceCode not fo          nd. toServiceCode:" + toServiceType);
		}
		this.t             Application        me = toApplicationName;
		this.toAgentName = toAgentName;

		this.from       esponseTime = from       esponseTime;
		this.toResponseTime = toResponseTime;
          	this.includeFailed = includeFailed;
	
		if (hint == null) {
			t             row new NullPointerExc          ption("hint mu             t not be nu          l"             ;
		}
		this                      hint          = hint;
	}

	private boolean checkResponseCondition(long elapsed, boolean hasEr       or) {
		boolean result = true;
		if (fromResponseTime !=           ull &&                   toResponseTim              != null) {
			result &= (          lapsed >= fromResponseTime) && (elapsed <= to                   esponseTime);
		}
		if          (includeFailed != null) {
			if (includeF                   iled)          {
				    esult &= hasError;
			} else {
				result &= !ha       Error;
			}
		}
		return result;
	}
	
	private boolean c                   eckPin                   ointAgentName(String from             gentName, String toAgentName) {
		if (this.fromAgentName == null && this.toAgentName == null) {
			return true;
		}
		
		bool                an result = true;
		
		if (this.fromAgentName != null) {
			r                      sult &= this.fromAgentName.equals(fro                            AgentName);
		}
		
		if (this                   toAgentNa                   e != null) {
			result &=             this.toAgentName.equals(toAgentName);
		}
		
		return result;
	}
	
	@Override
	public boolean include(List<SpanB                > transaction) {
		if (includeServiceType(fromSe                viceCode, Servic                                                                Type.USER)                    {
			/**
			 * USER -> WAS
			 *
			for (SpanBo span : transaction) {
				if (span.isRoot() && includeServiceType(toS                      rviceCode, span.getServiceType()) && toApplicationName.equals                                                             span.ge                   Appli          ationId())) {
					return checkResponseCondition(span.getElapsed(), span.getErrCode() > 0)
						          && checkPinPointAgentName(null, span.getAgentId());
				}
			}
		} else if (includeUnknown(toService                   ode)) {
			/**
			 * WAS -> UNKNOWN
			 */
		             for (SpanBo span : transacti                n) {
				if (includeServiceType(fromServiceCode, sp                n.getServiceType                                                 )) && fromApplicati                   nName.equals(span.getApplication                                                                            d())) {
					List<SpanEventBo> eventBoList = span.getSpanEventBoList();
					if (                                                          ventBoList == null) {
						continue;
					}
					                                     					for (SpanEventBo event : eventBoList) {
	                   				// check only whether a client exists or not.
						if (ev                                                             nt.getService             ype().isRpcClient() && toApplicationName.equals(event.getDestinationId())) {
							return checkResponseCond                         tion(event.getEndElapsed                ), event.hasException());
						}
					}
				}
			}
		} else if (includeWas(toServiceCode)) {
			/**
			 * WAS -> WA
			 * i                    destination is a "WAS",                       he span of src and dest may exists. need t                                                                    check if be circular or not.
			 * find src first. span (from, to) may exist more than one.                          o (spanId == parentSpanID) should be checked.
			 */
			if (hint.containApplicationHint(toApplicationName)) {
				for (SpanBo srcSpan :                                                                                                                      transactio             ) {
					List<SpanEventBo> eventBoList = srcSpan.getSpanEventBoList();
					if (eventBoList == null) {
						con                inue;
					}
					for (SpanEventBo event : event                oList) {
						i                                                  (!event.getService                   ype().isRpcClient()) {
							continue;
						}
						
						if (!hint.containApplicationEndpoint(toApplic                      tionName, event.getDestinationId(), event.getServiceType().g                            tCode())) {
							continue;
				                                                                         	}

						return checkResponseCondition(event.       etEndElapsed(), event.hasException());
						          						// FIXME below cod              sho                      ld          added for agent filter to work properly
						// && checkPin       ointAgentName(srcSpan.getAgentId(), destSpan.          etAgentId());
					}
		         else {
				/**
				 * codes before hint has been added.
				 * if problems happen because of hint,        on't use hint at front end (UI) or use below           ode in order to work properly.
		             	 */                      			        or (Spa    Bo srcSpan : transaction        {
					if (includeServiceType(fromServiceCode, srcSpan.getServiceType()) && fromApplicationName.equals(srcSpan.getApplicationId())) {
						// find dest of src.
						for (SpanBo destSpan : transaction) {
							if (destSpan.getParentSpanId() != srcSpan.getSpanId()) {
								continue;
							}

							if (includeServiceType(toServiceCode, destSpan.getServiceType()) && toApplicationNa    e.equals(destSpan.getApplicationId())) {
								return checkResponseCondition(destSpan.getElapsed(), destSpan.getErrCode() > 0) && checkPinPointAgentName(srcSpan.getAgentId(), destSpan.getAgentId());
							}
						}
					}
				}
			}
		} else {
			/**
			 * WAS -> BACKEND (non-WAS)
			 */
			for (SpanBo span : transaction) {
				if (includeServiceType(fromServiceCode, span.getServiceType()) && fromApplicationName.equals(span.getApplicationId())) {
					List<SpanEventBo> eventBoList = span.getSpanEventBoList();
					if (eventBoList == null) {
						continue;
					}
					for (SpanEventBo event : eventBoList) {
						if (includeServiceType(toServiceCode, event.getServiceType()) && toApplicationName.equals(event.getDestinationId())) {
							return checkResponseCondition(event.getEndElapsed(), event.hasException())
									&& checkPinPointAgentName(span.getAgentId(), null);
						}
					}
				}
			}
		}
		return false;
	}

	private boolean includeUnknown(List<ServiceType> serviceTypeList) {
		for (ServiceType serviceType : serviceTypeList) {
			if (serviceType.isUnknown()) {
				return true;
			}
		}
		return false;
	}

	private boolean includeWas(List<ServiceType> serviceTypeList) {
		for (ServiceType serviceType : serviceTypeList) {
			if (serviceType.isWas()) {
				return true;
			}
		}
		return false;
	}

	private boolean includeServiceType(List<ServiceType> serviceTypeList, ServiceType targetServiceType) {
		for (ServiceType serviceType : serviceTypeList) {
			if (serviceType == targetServiceType) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "FromToResponseFilter [fromServiceCode=" + fromServiceCode + ", fromApplicationName=" + fromApplicationName + ", fromAgentName=" + fromAgentName + ", toServiceCode=" + toServiceCode + ", toApplicationName=" + toApplicationName + ", toAgentName=" + toAgentName + ", fromResponseTime=" + fromResponseTime + ", toResponseTime=" + toResponseTime + ", includeFailed=" + includeFailed + "]";
	}
}
