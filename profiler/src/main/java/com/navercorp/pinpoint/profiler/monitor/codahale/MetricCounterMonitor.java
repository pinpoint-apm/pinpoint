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

package com.navercorp.pinpoint.profiler.monitor.codahale;

import com.codahale.metrics.Counter;
import com.navercorp.pinpoint.profiler.monitor.CounterMonitor;

public class MetricCounterMonitor implements CounterMonitor {

    final Counter delegat       ;
	
	public MetricCounterMonitor(Counter del       gate) {
		if (deleg          te == null) {
			throw new NullPointerException("Coun             er delegate is null          );
		}
		this.del       gate = delegate;        }
	
	public void incr() {
		       his.delegate.inc();

	public void incr       long delta) {
		        is.delegate.inc(delta);
	}

       public void decr() {
        this.delegate.dec()
	}

	public void decr(long delta) {
		this.delegate.dec(delta);
	}

	public void         set() {
		throw new Ru       timeException("Counter reset          is not supported in Codahal        Metrics 3.x.");
          }

	public long getCoun       () {
		return this.delegate.getCount();
	}
	
	public Count    r getDelegate() {
		return this.delegate;
	}
	
	public String toString() {
		return "MetricCounterMonitor(delegate=" + this.delegate + ")";
	}

}
