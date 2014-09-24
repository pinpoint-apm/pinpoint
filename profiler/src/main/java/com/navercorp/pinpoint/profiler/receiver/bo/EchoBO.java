package com.nhn.pinpoint.profiler.receiver.bo;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.profiler.receiver.TBaseRequestBO;
import com.nhn.pinpoint.thrift.dto.command.TCommandEcho;

/**
 * @author koo.taejin <kr14910>
 */
public class EchoBO implements TBaseRequestBO  {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public TBase<?, ?> handleRequest(TBase tbase) {
		logger.info("{} execute {}.", this, tbase);
		
		TCommandEcho param = (TCommandEcho) tbase;
		return param;
	}

}
