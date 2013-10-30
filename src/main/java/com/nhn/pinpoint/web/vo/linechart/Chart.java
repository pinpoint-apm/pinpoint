package com.nhn.pinpoint.web.vo.linechart;

import java.util.LinkedList;
import java.util.List;

public abstract class Chart {

	private String title;
	private String xAxisName;
	private String yAxisName;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setXAxisName(String name) {
		this.xAxisName = name;
	}

	public void setYAxisName(String name) {
		this.yAxisName = name;
	}

	public String getxAxisName() {
		return xAxisName;
	}

	public void setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}

	public String getyAxisName() {
		return yAxisName;
	}

	public void setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}
	
	public static final class Points {
		
		private List<Long[]> points = new LinkedList<Long[]>();

		public Points() {
		}

		public List<Long[]> getPoints() {
			return points;
		}

	}

}
