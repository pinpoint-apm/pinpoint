package com.nhn.pinpoint.web.vo.callstacks;

import com.nhn.pinpoint.common.ServiceType;

/**
 * each stack
 * 
 * @author netspider
 * 
 */
public class Record {
	private final int tab;
	private final int id;
	private final int pId;
	private final boolean method;

	private final String title;
    private String simpleClassName = "";
    private String fullApiDescription = "";

	private final String arguments;
	private final long begin;
	private final long elapsed;
	private final String agent;
	private final String service;
    private final ServiceType serviceType;
    private final String destinationId;
	private final boolean excludeFromTimeline;

    private boolean focused;
    private boolean hasChild;

	public Record(int tab, int id, int pId, boolean method, String title, String arguments, long begin, long elapsed, String agent, String service, ServiceType serviceType, String destinationId, boolean hasChild) {
		this.tab = tab;
		this.id = id;
		this.pId = pId;
		this.method = method;

		this.title = title;
		this.arguments = arguments;
		this.begin = begin;
		this.elapsed = elapsed;
		this.agent = agent;

		this.service = service;
        this.serviceType = serviceType;
        this.destinationId = destinationId;

		this.excludeFromTimeline = serviceType == null || serviceType.isInternalMethod();
		this.hasChild = hasChild;
	}

	public int getId() {
		return id;
	}

	public int getpId() {
		return pId;
	}

	public int getTab() {
		return tab;
	}
    public String getTabspace() {
        if(tab == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< tab; i++) {
            sb.append("&nbsp");
        }
        return sb.toString();
    }

	public boolean isMethod() {
		return method;
	}

	public String getTitle() {
		return title;
	}

	public String getArguments() {
		return arguments;
	}

	public long getBegin() {
		return begin;
	}

	public long getElapsed() {
		return elapsed;
	}

	public String getAgent() {
		return agent;
	}

	public String getService() {
		return service;
	}

    public String getApiType() {
        if (destinationId == null) {
            if (serviceType == null) {
                // parameter일 경우 serviceType이 없음.
                return "";
            }
            return serviceType.getDesc();
        }
        if (serviceType.isIncludeDestinationId()) {
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
        this.fullApiDescription = fullApiDescription;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean getHasChild() {
    	return hasChild;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Record{");
        sb.append("tab=").append(tab);
        sb.append(", id=").append(id);
        sb.append(", pId=").append(pId);
        sb.append(", method=").append(method);
        sb.append(", title='").append(title).append('\'');
        sb.append(", simpleClassName='").append(simpleClassName).append('\'');
        sb.append(", fullApiDescription='").append(fullApiDescription).append('\'');
        sb.append(", arguments='").append(arguments).append('\'');
        sb.append(", begin=").append(begin);
        sb.append(", elapsed=").append(elapsed);
        sb.append(", agent='").append(agent).append('\'');
        sb.append(", service='").append(service).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", destinationId='").append(destinationId).append('\'');
        sb.append(", excludeFromTimeline=").append(excludeFromTimeline);
        sb.append(", focused=").append(focused);
        sb.append(", hasChild=").append(hasChild);
        sb.append('}');
        return sb.toString();
    }
}
