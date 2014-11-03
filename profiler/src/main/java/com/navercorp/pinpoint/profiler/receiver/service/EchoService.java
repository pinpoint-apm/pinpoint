package com.nhn.pinpoint.profiler.receiver.service;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.nhn.pinpoint.thrift.dto.command.TCommandEcho;

/**
 * @author koo.taejin <kr14910>
 */
public class EchoService implements ProfilerRequestCommandService  {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public TBase<?, ?> requestCommandService(TBase tbase) {
		logger.info("{} execute {}.", this, tbase);
		
		TCommandEcho param = (TCommandEcho) tbase;
		return param;
	}
	
	@Override
	public Class<? extends TBase> getCommandClazz() {
		return TCommandEcho.class;
	}

}
