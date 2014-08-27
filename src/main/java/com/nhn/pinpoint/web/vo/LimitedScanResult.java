package com.nhn.pinpoint.web.vo;

/**
 * 
 * @author netspider
 * @author emeroad
 * @param <V>
 */
public class LimitedScanResult<V> {

    private long limitedTime;
    private V data;

	public V getScanData() {
		return data;
	}

	public void setScanData(V scanData) {
		this.data = scanData;
	}

	public long getLimitedTime() {
		return limitedTime;
	}

	public void setLimitedTime(long limitedTime) {
		this.limitedTime = limitedTime;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LimitedScanResult{");
        sb.append("limitedTime=").append(limitedTime);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
