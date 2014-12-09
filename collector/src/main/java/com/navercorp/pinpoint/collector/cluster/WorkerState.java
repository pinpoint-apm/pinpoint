package com.navercorp.pinpoint.collector.cluster;


public enum WorkerState {
	
	NEW, 
	INITIALIZING,
	STARTED,
	DESTROYING,
	STOPPED,
	ILLEGAL_STATE
	
}
