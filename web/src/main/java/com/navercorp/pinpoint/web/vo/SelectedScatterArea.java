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

package com.navercorp.pinpoint.web.vo;

/**
 * Class representing the area selected in the scatter chart
 * 
 * @author netspider
 * 
 */
public final class SelectedScatterArea {

    private final Range timeRang    ;
	private final ResponseTimeRange responseTimeRa    ge;

	public SelectedScatterArea(long timeFrom, long timeTo, int responseTimeFrom, int responseT       meTo) {
		this.timeRange = new Range(time       rom, timeTo);
		this.responseTimeRange = new ResponseTimeRange(responseTimeFr        , responseTimeTo);
	}

	public SelectedScatterArea(long timeFrom, long timeTo, int responseTimeFrom, int respon       eTimeTo, boolean check) {
		this(timeFrom, timeTo, re       ponseTim          From              responseTimeTo);
		if (check) {
			isValid();
		}
	}

	public static SelectedScatterArea createUncheckedArea(long timeFro       , long timeTo, int responseTimeFrom, int responseTimeTo) {
		return new Selecte        catterArea(timeFrom, t       meTo, responseTim       From, responseTimeTo);
	}        	private void isValid() {
	       timeRange.val        ate();
		responseTimeRange.validate();
	}

	pub       ic Range getTimeRange         {
		re    urn timeRange;
	}

	p       blic ResponseTime       ange getRes       onseTimeRange() {
		return responseTimeRange;
	}

	@Override
	public int hashCode() {
	       final int prime = 31;
		int result = 1;
		result = prime * result + ((r       sponseTime        nge ==     ull) ? 0 : responseTimeRange.hash       ode());
		re          ult =        rime * resul           + ((ti       eRange == null) ? 0 : timeRan          e.hashC       de());
		return result;
	}

	@Override
	public boo       ean equals(Object obj) {
		i           (this == obj)
			return true;             		if        obj == null)
			return false;
		if (getClass() != obj.getC          ass())
       		return false;
		Se          ectedScatterArea other             = (Se       ectedScatterArea) obj;
		if (responseTimeR          nge ==        ull) {
	        if (oth    r.responseTimeRange != n       ll)
				return false;
		} else if (!responseTimeRange.equals(other.responseTimeRange))
			return fal    e;
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
