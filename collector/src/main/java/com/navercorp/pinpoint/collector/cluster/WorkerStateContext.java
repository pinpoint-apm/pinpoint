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

package com.navercorp.pinpoint.collector.cluster;

import java.util.concurrent.atomic.AtomicReference;

public class WorkerStateContext {

	private final AtomicReference<WorkerState> currentState = new AtomicReference<WorkerState    ();

	public WorkerStateCon       ext() {
		currentState.set(Wor          erState.NEW);
	}
	
	public WorkerSt       te getCurrentState() {          		return currentState.get();
	}
	
	publ       c boolean changeStateInitializing() {
		return currentState.compareAndSet          WorkerState.NEW, WorkerState.INITI       LIZING);
	}
	
	public boolean changeStateStarted() {
		return currentState.co        areAndSet(WorkerState.INITIALIZING, Wo       kerState.STARTED);
	}

	public boolean changeStateDestroying() {
		return c        rentState.compareAndSet(WorkerState       STARTED, WorkerState.DESTROYING);
	}

	public boolean changeStateStopped()
		return currentState.compareAndS       t(WorkerState.DESTROYING, WorkerState.ST       PPED);

	public boolean changeSt       teIllegal() {
		currentState.set(WorkerState          ILLEGA       _STA          E);
		r          turn true;
	}

	public boolean isStarted() {
		if (currentState.get() == WorkerState.STARTED) {
			return true;
		} else {
			return false;
		}
	}
}
