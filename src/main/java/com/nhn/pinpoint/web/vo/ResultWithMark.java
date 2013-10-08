package com.nhn.pinpoint.web.vo;

/**
 * 
 * @author netspider
 * 
 * @param <V>
 * @param <M>
 */
public class ResultWithMark<V, M> {

	private V value;
	private M mark;

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public M getMark() {
		return mark;
	}

	public void setMark(M mark) {
		this.mark = mark;
	}

	@Override
	public String toString() {
		return "ResultWithMark [value=" + value + ", mark=" + mark + "]";
	}
}
