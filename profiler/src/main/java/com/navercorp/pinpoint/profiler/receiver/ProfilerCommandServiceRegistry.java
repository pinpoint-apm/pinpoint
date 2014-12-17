package com.navercorp.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

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
        if (service == null) {
            throw new NullPointerException("service must not be null");
        }
        return addService(service.getCommandClazz(), service);
    }

    public boolean addService(Class<? extends TBase> clazz, ProfilerCommandService service) {
        if (clazz == null) {
            throw new NullPointerException("clazz must not be null");
        }
        if (service == null) {
            throw new NullPointerException("service must not be null");
        }

        final ProfilerCommandService before = profilerCommandServiceRepository.putIfAbsent(clazz, service);
        if (before != null) {
            logger.warn("Already Register Type({}).", clazz.getName());
            return false;
        }

        return true;
    }

    @Override
    public ProfilerCommandService getService(TBase tBase) {
        if (tBase == null) {
            return null;
        }
        return profilerCommandServiceRepository.get(tBase.getClass());
    }

    @Override
    public ProfilerSimpleCommandService getSimpleService(TBase tBase) {
        if (tBase == null) {
            return null;
        }

        final ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
        if (service instanceof ProfilerSimpleCommandService) {
            return (ProfilerSimpleCommandService) service;
        }

        return null;
    }

    @Override
    public ProfilerRequestCommandService getRequestService(TBase tBase) {
        if (tBase == null) {
            return null;
        }

        final ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
        if (service instanceof ProfilerRequestCommandService) {
            return (ProfilerRequestCommandService) service;
        }

        return null;
    }

    @Override
    public ProfilerStreamCommandService getStreamService(TBase tBase) {
        if (tBase == null) {
            return null;
        }

        final ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
        if (service instanceof ProfilerStreamCommandService) {
            return (ProfilerStreamCommandService) service;
        }

        return null;
    }

}
