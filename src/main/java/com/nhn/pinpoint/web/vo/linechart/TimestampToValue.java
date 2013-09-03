package com.nhn.pinpoint.web.vo.linechart;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXME 일반화 해야 할까?
 * @author harebox
 */
public class TimestampToValue {
	private List<Long> x; // timestamp
	private List<Long> y; // value
	
	public TimestampToValue() {
		this.x = new ArrayList<Long>();
		this.y = new ArrayList<Long>();
	}
	
	public void addData(long xv, long yv) {
		if (x.size() != y.size()) {
			throw new InvalidParameterException("invalid status : x-axis.size=" + x.size() + ", y-axis.size=" + y.size());
		}
		this.x.add(xv);
		this.y.add(yv);
	}

	public List<Long> getX() {
		return x;
	}

	public void setX(List<Long> x) {
		this.x = x;
	}

	public List<Long> getY() {
		return y;
	}

	public void setY(List<Long> y) {
		this.y = y;
	}
	
}