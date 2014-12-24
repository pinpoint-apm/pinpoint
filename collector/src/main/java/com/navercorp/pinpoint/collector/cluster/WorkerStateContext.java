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
	
	private final AtomicReference<WorkerState> currentState = new AtomicReference<WorkerState>();

	public WorkerStateContext() {
		currentState.set(WorkerState.NEW);
	}
	
	public WorkerState getCurrentState() {
		return currentState.get();
	}
	
	public boolean changeStateInitializing() {
		return currentState.compareAndSet(WorkerState.NEW, WorkerState.INITIALIZING);
	}
	
	public boolean changeStateStarted() {
		return currentState.compareAndSet(WorkerState.INITIALIZING, WorkerState.STARTED);
	}

	public boolean changeStateDestroying() {
		return currentState.compareAndSet(WorkerState.STARTED, WorkerState.DESTROYING);
	}

	public boolean changeStateStoped() {
		return currentState.compareAndSet(WorkerState.DESTROYING, WorkerState.STOPPED);
	}
	
	public boolean changeStateIllegal() {
		currentState.set(WorkerState.ILLEGAL_STATE);
		return true;
	}

	public boolean isStarted() {
		if (currentState.get() == WorkerState.STARTED) {
			return true;
		} else {
			return false;
		}
	}
}
