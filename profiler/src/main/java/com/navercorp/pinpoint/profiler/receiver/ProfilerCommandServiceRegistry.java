package com.nhn.pinpoint.profiler.receiver;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
public class ProfilerCommandServiceRegistry implements ProfilerCommandServiceLocator {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ConcurrentHashMap<Class<? extends TBase>, ProfilerCommandService> profilerCommandServiceRepository;

	public ProfilerCommandServiceRegistry() {
		profilerCommandServiceRepository = new ConcurrentHashMap<Class<? extends TBase>, ProfilerCommandService>();
	}
	
	public boolean addService(ProfilerCommandService service) {
		return addService(service.getCommandClazz(), service);
	}
	
	public boolean addService(Class<? extends TBase> clazz, ProfilerCommandService service) {
	    ProfilerCommandService inValue = profilerCommandServiceRepository.putIfAbsent(clazz, service);
	    
	    if (inValue != null) {
            logger.warn("Already Register Type({}).", clazz.getName());
            return false;
	    }
	    
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
	
	@Override
	public ProfilerStreamCommandService getStreamService(TBase tBase) {
        ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
        
        if (service instanceof ProfilerStreamCommandService) {
            return (ProfilerStreamCommandService) service;
        }
        
        return null;
	}

}
