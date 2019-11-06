/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common.profiler.trace;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.DefaultServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 */
public class TraceMetadataLoader {

    private final CommonLogger logger;

    private final List<ServiceType> staticServiceTypes;
    private final List<AnnotationKey> staticAnnotationKeys;
    private final List<DisplayArgumentMatcher> staticDisplayArgumentMatchers;

    private final List<ServiceTypeInfo> serviceTypeInfos = new ArrayList<ServiceTypeInfo>();
    private final ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker();

    private final List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>();
    private final AnnotationKeyChecker annotationKeyChecker = new AnnotationKeyChecker();

    public TraceMetadataLoader(CommonLoggerFactory loggerFactory) {
        if (loggerFactory == null) {
            throw new NullPointerException("loggerFactory");
        }
        this.logger = loggerFactory.getLogger(TraceMetadataLoader.class.getName());
        this.staticServiceTypes = staticFieldLookUp(ServiceType.class, ServiceType.class);
        this.staticAnnotationKeys = staticFieldLookUp(AnnotationKey.class, AnnotationKey.class);
        this.staticDisplayArgumentMatchers = staticFieldLookUp(DefaultDisplayArgument.class, DisplayArgumentMatcher.class);
    }

    private <T> List<T> staticFieldLookUp(Class<?> targetClazz, Class<T> lookUpClazz) {
        StaticFieldLookUp<T> staticFieldLookUp = new StaticFieldLookUp<T>(targetClazz, lookUpClazz);
        return Collections.unmodifiableList(staticFieldLookUp.lookup());
    }

    public void load(List<TraceMetadataProvider> providers) {
        if (providers == null) {
            throw new NullPointerException("providers");
        }

        logger.info("Loading TraceMetadataProviders");

        for (TraceMetadataProvider provider : providers) {
            if (logger.isInfoEnabled()) {
                logger.info("Loading TraceMetadataProvider: " + provider.getClass().getName() + " from:" + provider.toString());
            }

            TraceMetadataSetupContextImpl context = new TraceMetadataSetupContextImpl(provider);
            provider.setup(context);
        }

        this.serviceTypeChecker.logResult();
        this.annotationKeyChecker.logResult();
    }

    public ServiceTypeRegistry createServiceTypeRegistry() {
        logInfo("creating ServiceTypeRegistry");
        ServiceTypeRegistry.Builder builder = new ServiceTypeRegistry.Builder();
        for (ServiceType serviceType : staticServiceTypes) {
            logInfo("add Default ServiceType:" + serviceType.getName());
            builder.addServiceType(serviceType);
        }
        for (ServiceTypeInfo serviceTypeInfo : serviceTypeInfos) {
            ServiceType serviceType = serviceTypeInfo.getServiceType();
            logInfo("add Plugin ServiceType:" + serviceType.getName());
            builder.addServiceType(serviceType);
        }
        return builder.build();
    }

    public AnnotationKeyRegistry createAnnotationKeyRegistry() {
        logInfo("creating AnnotationKeyRegistry");
        AnnotationKeyRegistry.Builder builder = new AnnotationKeyRegistry.Builder();
        for (AnnotationKey annotationKey : staticAnnotationKeys) {
            logInfo("add Default AnnotationKey:" + annotationKey);
            builder.addAnnotationKey(annotationKey);
        }
        for (AnnotationKey annotationKey : annotationKeys) {
            logInfo("add PluginAnnotationKey:" + annotationKey);
            builder.addAnnotationKey(annotationKey);
        }
        return builder.build();
    }

    public AnnotationKeyMatcherRegistry createAnnotationKeyMatcherRegistry() {
        if (logger.isDebugEnabled()) {
            logger.debug("creating AnnotationKeyMatcherRegistry");
        }
        AnnotationKeyMatcherRegistry.Builder builder = new AnnotationKeyMatcherRegistry.Builder();
        for (DisplayArgumentMatcher displayArgumentMatcher : staticDisplayArgumentMatchers) {
            AnnotationKeyMatcher annotationKeyMatcher = displayArgumentMatcher.getAnnotationKeyMatcher();
            if (annotationKeyMatcher != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("add DefaultAnnotationKeyMatcher ServiceType:" + displayArgumentMatcher.getServiceType() + ", AnnotationKeyMatcher:" + annotationKeyMatcher);
                }
                builder.addAnnotationKeyMatcher(displayArgumentMatcher.getServiceType(), annotationKeyMatcher);
            }
        }
        for (ServiceTypeInfo serviceTypeInfo : serviceTypeInfos) {
            if (serviceTypeInfo.getPrimaryAnnotationKeyMatcher() != null) {
                ServiceType serviceType = serviceTypeInfo.getServiceType();
                AnnotationKeyMatcher primaryAnnotationKeyMatcher = serviceTypeInfo.getPrimaryAnnotationKeyMatcher();
                if (logger.isDebugEnabled()) {
                    logger.debug("add AnnotationKeyMatcher ServiceType:" + serviceType + ", AnnotationKeyMatcher:" + primaryAnnotationKeyMatcher);
                }
                builder.addAnnotationKeyMatcher(serviceType, primaryAnnotationKeyMatcher);
            }
        }
        return builder.build();
    }

    private void logInfo(String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }

    private class TraceMetadataSetupContextImpl implements TraceMetadataSetupContext {
        private final TraceMetadataProvider provider;

        public TraceMetadataSetupContextImpl(TraceMetadataProvider provider) {
            this.provider = provider;
        }

        @Override
        public void addServiceType(ServiceType serviceType) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType");
            }
            ServiceTypeInfo type = new DefaultServiceTypeInfo(serviceType);
            addType0(type);
        }

        @Override
        public void addServiceType(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType");
            }
            if (annotationKeyMatcher == null) {
                throw new NullPointerException("annotationKeyMatcher");
            }
            ServiceTypeInfo type = new DefaultServiceTypeInfo(serviceType, annotationKeyMatcher);
            addType0(type);
        }

        private void addType0(ServiceTypeInfo type) {
            if (type == null) {
                throw new NullPointerException("type");
            }
            // local check
            serviceTypeChecker.check(type.getServiceType(), provider);
            serviceTypeInfos.add(type);
        }

        @Override
        public void addAnnotationKey(AnnotationKey annotationKey) {
            if (annotationKey == null) {
                throw new NullPointerException("annotationKey");
            }
            // local check
            annotationKeyChecker.check(annotationKey, provider);
            annotationKeys.add(annotationKey);
        }
    }

    private static String serviceTypePairToString(Pair<ServiceType> pair) {
        return pair.value.getName() + "(" + pair.value.getCode() + ") from " + pair.provider.toString();
    }

    private static String annotationKeyPairToString(Pair<AnnotationKey> pair) {
        return pair.value.getName() + "(" + pair.value.getCode() + ") from " + pair.provider.toString();
    }

    private static class Pair<T> {
        private final T value;
        private final TraceMetadataProvider provider;
        
        public Pair(T value, TraceMetadataProvider provider) {
            this.value = value;
            this.provider = provider;
        }
    }
    
    private class ServiceTypeChecker {
        private final Map<String, Pair<ServiceType>> serviceTypeNameMap = new HashMap<String, Pair<ServiceType>>();
        private final Map<Short, Pair<ServiceType>> serviceTypeCodeMap = new HashMap<Short, Pair<ServiceType>>();

        private void check(ServiceType type, TraceMetadataProvider provider) {
            Pair<ServiceType> pair = new Pair<ServiceType>(type, provider);
            Pair<ServiceType> prev = serviceTypeNameMap.put(type.getName(), pair);
    
            if (prev != null) {
                // TODO change exception type
                throw new RuntimeException("ServiceType name of " + serviceTypePairToString(pair) + " is duplicated with " + serviceTypePairToString(prev));
            }
    
            prev = serviceTypeCodeMap.put(type.getCode(), pair);
    
            if (prev != null) {
                // TODO change exception type
                throw new RuntimeException("ServiceType code of " + serviceTypePairToString(pair) + " is duplicated with " + serviceTypePairToString(prev));
            }

            if(type.isAlias()){
                if(!type.isRpcClient()){
                    throw new RuntimeException("ServiceType code of " + serviceTypePairToString(pair) + " should be between range of RPC");
                }
                if(type.isRecordStatistics()){
                    throw new RuntimeException("ServiceType code of " + serviceTypePairToString(pair) + " can't have ALIAS and RECORD_STATISTICS at the same time");
                }
            }
        }

        private void logResult() {
            logger.info("Finished loading ServiceType:");

            List<Pair<ServiceType>> serviceTypes = new ArrayList<Pair<ServiceType>>(serviceTypeCodeMap.values());
            Collections.sort(serviceTypes, new Comparator<Pair<ServiceType>>() {
                @Override
                public int compare(Pair<ServiceType> o1, Pair<ServiceType> o2) {
                    short code1 = o1.value.getCode();
                    short code2 = o2.value.getCode();

                    return code1 > code2 ? 1 : (code1 < code2 ? -1 : 0);
                }
            });

            for (Pair<ServiceType> serviceType : serviceTypes) {
                logger.info(serviceTypePairToString(serviceType));
            }
        }
    }

    private class AnnotationKeyChecker {
        private final Map<Integer, Pair<AnnotationKey>> annotationKeyCodeMap = new HashMap<Integer, Pair<AnnotationKey>>();

        private void check(AnnotationKey key, TraceMetadataProvider provider) {
            Pair<AnnotationKey> pair = new Pair<AnnotationKey>(key, provider);
            Pair<AnnotationKey> prev = annotationKeyCodeMap.put(key.getCode(), pair);
    
            if (prev != null) {
                // TODO change exception type
                throw new RuntimeException("AnnotationKey code of " + annotationKeyPairToString(pair) + " is duplicated with " + annotationKeyPairToString(prev));
            }
        }

        private void logResult() {
            logger.info("Finished loading AnnotationKeys:");

            List<Pair<AnnotationKey>> annotationKeys = new ArrayList<Pair<AnnotationKey>>(annotationKeyCodeMap.values());
            Collections.sort(annotationKeys, new Comparator<Pair<AnnotationKey>>() {
                @Override
                public int compare(Pair<AnnotationKey> o1, Pair<AnnotationKey> o2) {
                    int code1 = o1.value.getCode();
                    int code2 = o2.value.getCode();

                    return code1 > code2 ? 1 : (code1 < code2 ? -1 : 0);
                }
            });

            for (Pair<AnnotationKey> annotationKey : annotationKeys) {
                logger.info(annotationKeyPairToString(annotationKey));
            }
        }
    }
}
