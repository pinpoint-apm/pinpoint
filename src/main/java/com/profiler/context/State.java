package com.profiler.context;

import java.util.List;

import com.profiler.context.tracer.Tracer;

public class State {

	private TraceID id;
	private boolean terminal;
	private final List<Tracer> tracers;

	public State(TraceID id, boolean terminal, List<Tracer> tracers) {
		this.id = id;
		this.terminal = terminal;
		this.tracers = tracers;
	}

	public TraceID getId() {
		return id;
	}

	public boolean isTerminal() {
		return terminal;
	}

	public List<Tracer> getTracers() {
		return tracers;
	}

	public boolean addTracer(Tracer tracer) {
		return tracers.add(tracer);
	}

	public void setId(TraceID id) {
		this.id = id;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	@Override
	public String toString() {
		return "traceId=" + id + ", isTerminal=" + terminal + ", tracers=" + tracers;
	}

}
