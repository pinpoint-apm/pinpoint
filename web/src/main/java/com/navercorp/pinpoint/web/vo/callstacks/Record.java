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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.common.ServiceType;

/**
 * each stack
 * 
 * @author netspider
 * @author emeroad
 */
public class Record {
    private final int ta    ;
	private final in     id;
	private final int p    rentId;
	private final boole    n method;

	private final String title;
    private String simpleClassName = "";
    private String fullApiDesc    iption = "";

	private final     tring arguments;
	priva    e final long begin;
	private final long elapsed;
        rivate final long gap;
	p    ivate final String agent;
	private final String applicationName;
    private final ServiceType serviceType;
    privat     final String destinationId;
	private final boolean excludeFromTimeline;

    private boolean focused;
    private boolean hasChild;
       private boolean hasException;

	public Record(int tab, int id, int parentId, boolean method, String title, String arguments, long begin, long elapsed, long gap, String agent, String applicationName, ServiceType serviceType, String destinationId, boolea        hasChild,        oolean ha       Exception) {
		this.t       b = tab;
		this.id       = id;
		this.pa       entId = parentId;
		thi       .method = metho       ;

		this.title = title;
		this.arguments =       arguments;
		thi       .begin = begin;
		this.elapsed = elapsed;
        this.gap = gap;
		this.agent = agent;

		this.applicationName = applic       tionName;
        this.serviceType = serviceType;
        this.destinationId         destinationId;

		th       s.excludeFromTimeline = servi        Type == null || se       viceTy        .isInternalMethod();
		t       is.hasChild         hasChild;
		this.ha       Excepti    n = hasException;
	}

	public int getId() {
		return id;
	}

	public int getParentId() {
		return parentId;
	}

	public int getTab() {
		return tab;
	}
    public String getTabspace() {
        if(tab == 0) {
            return "";
        }
        St    ingBuilder sb = new Strin       Builder();               for(int i=0; i< tab; i++) {
                   b.append("&nbsp");
        }
        return sb.toStrin        );
    }

	public bool       an isMeth        () {
		return method;
	}
	public St    ing getTitle() {
        return title;
	}

	public String getArguments() {
        re       urn argum        ts;
	}

	public long getBegin() {
       	return begin;
	}

    public long getElapsed() {
		return elapsed;
	}

    public long getGap() {
        return gap;
    }

    public String getAgent() {
		return agent;
	}

	public String getApplicationName() {
		return applicationName;
	}

    public String getApiType() {
        if (destinationId == null) {
            if (serviceType == null) {
                // no ServiceType when parameter
                return "";
            }
            return serviceType.getDesc();
        }
        if (serviceTyp       .isIncludeDestinationId    )) {
            return serviceType.getDesc() + "(" + destinationId + ")";
        } else {
            return serviceType.getDesc();
        }

    }

    public boolean isExcludeFromTimeline() {
		return excludeFromTimeline;
	}

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public void setSimpleClassName(String simpleClassName) {
        this.simpleClassName = simpleClassName;
    }

    public String getFullApiDescription() {
        return fullApiDescription;
    }

    public void setFullApiDescription(String fullApiDescription) {
        this.fullA    iDescription = fullApiDescription;
    }

    public boolean isFocus    d() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean getHasChild() {
    	return hasChild;
    }
    
    public boolean getHasException() {
    	return hasException;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Record{");
        sb.append("tab=").append(tab);
        sb.append(", id=").append(id);
        sb.append(", parentId=").append(parentId);
        sb.append(", method=").append(method);
        sb.append(", title='").append(title).append('\'');
        sb.append(", simpleClassName='").append(simpleClassName).append('\'');
        sb.append(", fullApiDescription='").append(fullApiDescription).append('\'');
        sb.append(", arguments='").append(arguments).append('\'');
        sb.append(", begin=").append(begin);
        sb.append(", elapsed=").append(elapsed);
        sb.append(", gap=").append(gap);
        sb.append(", agent='").append(agent).append('\'');
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", destinationId='").append(destinationId).append('\'');
        sb.append(", excludeFromTimeline=").append(excludeFromTimeline);
        sb.append(", focused=").append(focused);
        sb.append(", hasChild=").append(hasChild);
        sb.append('}');
        return sb.toString();
    }
}
