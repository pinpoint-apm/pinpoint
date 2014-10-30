package com.nhn.pinpoint.profiler.receiver;

import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
public class ProfilerCommandServiceRegistry implements ProfilerCommandServiceLocator {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<Class<? extends TBase>, ProfilerCommandService> profilerCommandServiceRepository;

	public ProfilerCommandServiceRegistry() {
		profilerCommandServiceRepository = new HashMap<Class<? extends TBase>, ProfilerCommandService>();
	}

	/**
	 * not guarantee thread safe.
	 */
	
	public boolean addService(ProfilerCommandService service) {
		return addService(service.getCommandClazz(), service);
	}
	
	public boolean addService(Class<? extends TBase> clazz, ProfilerCommandService service) {
		if (profilerCommandServiceRepository.containsKey(clazz)) {
			logger.warn("Already Register Type({}).", clazz.getName());
			return false;
		}

		profilerCommandServiceRepository.put(clazz, service);
		return true;
	}

	@Override
	public ProfilerCommandService getService(TBase tBase) {
		return profilerCommandServiceRepository.get(tBase.getClass());
	}

	@Override
	public ProfilerSimpleCommandService getSimpleService(TBase tBase) {
		ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
		
		if (service instanceof ProfilerSimpleCommandService) {
			return (ProfilerSimpleCommandService) service;
		}
		
		return null;
	}

	@Override
	public ProfilerRequestCommandService getRequestService(TBase tBase) {
		ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
		
		if (service instanceof ProfilerRequestCommandService) {
			return (ProfilerRequestCommandService) service;
		}
		
		return null;
	}

}
