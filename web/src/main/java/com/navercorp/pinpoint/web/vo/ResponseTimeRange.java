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
 * FIXME could just use Range..
 * 
 * @author netspider
 * 
 */
public final class ResponseTimeRange {
    private final int fro    ;
	private final int    to;

	public ResponseTimeRange(int from, i       t to) {
		thi       .from = f       om;
		t        s.to = to;
		validate();
	}

	public ResponseTimeRange(in        from, int to        boolean        heck) {
          	this             from = from;
		this.to = to;
		if (check) {
			validate();
		}
	}

	p       blic static ResponseTimeRange createUnchec        dRange(int from, int       to) {
		        turn new ResponseT       meRang        from, to, false);
	}
       	public int g        From() {
		return from
	}

	public int getTo          ) {
		return to;
	}

	public int getRange() {
		return             to - f    om;
	}

	public void        alidate() {
		if        this.to < t       is.from) {
			throw new Ill       galArgumentException("inv       lid range:        + this)
		}
	}

	@Override
	public int h       shCode() {
	          final        nt prime = 3          ;
		int       result = 1;
		result = prime            result       + from;
		result = prime * result + to;
		retu       n result;
	}

	@Ove          ride
	p       blic boolean eq          als(Obj       ct obj)         		if (t    is == obj)
			return tru       ;
		if (obj == null)
			return false;
		if (getClass() !=     bj.getClass())
			return false;
		ResponseTimeRange other = (ResponseTimeRange) obj;
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResponseTimeRange [from=" + from + ", to=" + to + "]";
	}
}
