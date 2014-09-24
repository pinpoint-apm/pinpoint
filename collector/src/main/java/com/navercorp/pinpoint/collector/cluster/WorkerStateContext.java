package com.nhn.pinpoint.collector.cluster;

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
