package com.nhn.pinpoint.web.vo;

/**
 * scatter chart에서 마우스로 드래그해서 선택한 영역의 정보
 * 
 * @author netspider
 * 
 */
public final class SelectedScatterArea {

	private final Range timeRange;
	private final ResponseTimeRange responseTimeRange;

	public SelectedScatterArea(long timeFrom, long timeTo, int responseTimeFrom, int responseTimeTo) {
		this.timeRange = new Range(timeFrom, timeTo);
		this.responseTimeRange = new ResponseTimeRange(responseTimeFrom, responseTimeTo);
	}

	public SelectedScatterArea(long timeFrom, long timeTo, int responseTimeFrom, int responseTimeTo, boolean check) {
		this(timeFrom, timeTo, responseTimeFrom, responseTimeTo);
		if (check) {
			isValid();
		}
	}

	public static SelectedScatterArea createUncheckedArea(long timeFrom, long timeTo, int responseTimeFrom, int responseTimeTo) {
		return new SelectedScatterArea(timeFrom, timeTo, responseTimeFrom, responseTimeTo);
	}

	private void isValid() {
		timeRange.validate();
		responseTimeRange.validate();
	}

	public Range getTimeRange() {
		return timeRange;
	}

	public ResponseTimeRange getResponseTimeRange() {
		return responseTimeRange;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((responseTimeRange == null) ? 0 : responseTimeRange.hashCode());
		result = prime * result + ((timeRange == null) ? 0 : timeRange.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectedScatterArea other = (SelectedScatterArea) obj;
		if (responseTimeRange == null) {
			if (other.responseTimeRange != null)
				return false;
		} else if (!responseTimeRange.equals(other.responseTimeRange))
			return false;
		if (timeRange == null) {
			if (other.timeRange != null)
				return false;
		} else if (!timeRange.equals(other.timeRange))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SelectedScatterArea [timeRange=" + timeRange + ", responseTimeRange=" + responseTimeRange + "]";
	}
}
