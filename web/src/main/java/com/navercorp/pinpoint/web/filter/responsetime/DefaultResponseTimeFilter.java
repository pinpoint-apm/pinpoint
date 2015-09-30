package com.navercorp.pinpoint.web.filter.responsetime;


/**
 * @author emeroad
 */
public class DefaultResponseTimeFilter implements ResponseTimeFilter {

    private final long fromResponseTime;
    private final long toResponseTime;

    public DefaultResponseTimeFilter(long fromResponseTime, long toResponseTime) {
        this.fromResponseTime = fromResponseTime;
        this.toResponseTime = toResponseTime;
    }

    @Override
    public boolean accept(long elapsed) {
        if ((elapsed >= fromResponseTime) && (elapsed <= toResponseTime) ) {
            return ACCEPT;
        }
        return  REJECT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultResponseTimeFilter{");
        sb.append("fromResponseTime=").append(fromResponseTime);
        sb.append(", toResponseTime=").append(toResponseTime);
        sb.append('}');
        return sb.toString();
    }
}
