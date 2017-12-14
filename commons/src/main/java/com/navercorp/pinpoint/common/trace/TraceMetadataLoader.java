/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.common.trace;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

/**
 * @author Jongho Moon
 *
 */
public class TraceMetadataLoader {

    private final CommonLogger logger;

    private final List<ServiceTypeInfo> serviceTypeInfos = new ArrayList<ServiceTypeInfo>();
    private final ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker();

    private final List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>();
    private final AnnotationKeyChecker annotationKeyChecker = new AnnotationKeyChecker();

    public TraceMetadataLoader() {
        this(StdoutCommonLoggerFactory.INSTANCE);
    }

    public TraceMetadataLoader(CommonLoggerFactory loggerFactory) {
        if (loggerFactory == null) {
            throw new NullPointerException("loggerFactory must not be null");
        }
        this.logger = loggerFactory.getLogger(TraceMetadataLoader.class.getName());
    }

    public void load(URL[] urls) {
        if (urls == null) {
            throw new NullPointerException("urls must not be null");
        }

        List<TraceMetadataProvider> providers = PluginLoader.load(TraceMetadataProvider.class, urls);
        load(providers);
    }
    
    public void load(ClassLoader loader) {
        if (loader == null) {
            throw new NullPointerException("loader must not be null");
        }

        List<TraceMetadataProvider> providers = PluginLoader.load(TraceMetadataProvider.class, loader);
        load(providers);
    }
    
    public void load(List<TraceMetadataProvider> providers) {
        if (providers == null) {
            throw new NullPointerException("providers must not be null");
        }

        logger.info("Loading TraceMetadataProviders");

        for (TraceMetadataProvider provider : providers) {
            if (logger.isInfoEnabled()) {
                logger.info("Loading TraceMetadataProvider: " + provider.getClass().getName() + " name:" + provider.toString());
            }

            TraceMetadataSetupContextImpl context = new TraceMetadataSetupContextImpl(provider.getClass());
            provider.setup(context);
        }

        this.serviceTypeChecker.logResult();
        this.annotationKeyChecker.logResult();
    }

    public List<ServiceTypeInfo> getServiceTypeInfos() {
        return serviceTypeInfos;
    }

    public List<AnnotationKey> getAnnotationKeys() {
        return annotationKeys;
    }


    private class TraceMetadataSetupContextImpl implements TraceMetadataSetupContext {
        private final Class<?> provider;

        public TraceMetadataSetupContextImpl(Class<?> provider) {
            this.provider = provider;
        }

        @Override
        public void addServiceType(ServiceType serviceType) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType must not be null");
            }
            ServiceTypeInfo type = new DefaultServiceTypeInfo(serviceType);
            addType0(type);
        }

        @Override
        public void addServiceType(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType must not be null");
            }
            if (annotationKeyMatcher == null) {
                throw new NullPointerException("annotationKeyMatcher must not be null");
            }
            ServiceTypeInfo type = new DefaultServiceTypeInfo(serviceType, annotationKeyMatcher);
            addType0(type);
        }

        private void addType0(ServiceTypeInfo type) {
            if (type == null) {
                throw new NullPointerException("type must not be null");
            }
            // local check
            serviceTypeChecker.check(type.getServiceType(), provider);
            serviceTypeInfos.add(type);
        }

        @Override
        public void addAnnotationKey(AnnotationKey annotationKey) {
            if (annotationKey == null) {
                throw new NullPointerException("annotationKey must not be null");
            }
            // local check
            annotationKeyChecker.check(annotationKey, provider);
            annotationKeys.add(annotationKey);
        }
    }


    private static String serviceTypePairToString(Pair<ServiceType> pair) {
        return pair.value.getName() + "(" + pair.value.getCode() + ") from " + pair.provider.getName();
    }

    private static String annotationKeyPairToString(Pair<AnnotationKey> pair) {
        return pair.value.getName() + "(" + pair.value.getCode() + ") from " + pair.provider.getName();
    }

    private static class Pair<T> {
        private final T value;
        private final Class<?> provider;
        
        public Pair(T value, Class<?> provider) {
            this.value = value;
            this.provider = provider;
        }
    }
    
    private class ServiceTypeChecker {
        private final Map<String, Pair<ServiceType>> serviceTypeNameMap = new HashMap<String, Pair<ServiceType>>();
        private final Map<Short, Pair<ServiceType>> serviceTypeCodeMap = new HashMap<Short, Pair<ServiceType>>();

        private void check(ServiceType type, Class<?> providerClass) {
            Pair<ServiceType> pair = new Pair<ServiceType>(type, providerClass);
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

        private void check(AnnotationKey key, Class<?> providerClass) {
            Pair<AnnotationKey> pair = new Pair<AnnotationKey>(key, providerClass);
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
