package com.nhn.pinpoint.web.vo.linechart;

import java.util.List;

public class LineChart extends Chart {

	protected Chart.Points points;

	public LineChart() {
		this.points = new Points();
	}
	
	public void addPoint(Long[] point) {
		points.getPoints().add(point);
	}

	public void setPoints(Points points) {
		this.points = points;
	}
	
	public List<Long[]> getPoints() {
		return this.points.getPoints();
	}

}
