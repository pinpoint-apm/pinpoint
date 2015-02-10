/**
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
package com.navercorp.pinpoint.common;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.plugin.ServiceTypeProvider;
import com.navercorp.pinpoint.common.plugin.ServiceTypeSetupContext;

/**
 * @author Jongho Moon
 *
 */
public class ServiceTypeProviderLoader {
    private final Logger logger = Logger.getLogger(getClass().getName());
    
    private final ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker();
    private final AnnotationKeyChecker annotationKeyChecker = new AnnotationKeyChecker();
    
    private final List<ServiceType> serviceTypes = new ArrayList<ServiceType>();
    private final List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>();
    
    public void load(URL[] urls) {
        List<ServiceTypeProvider> providers = PluginLoader.load(ServiceTypeProvider.class, urls);
        load(providers);
    }
    
    public void load(ClassLoader loader) {
        List<ServiceTypeProvider> providers = PluginLoader.load(ServiceTypeProvider.class, loader);
        load(providers);
    }
    
    void load(List<ServiceTypeProvider> providers) {
        logger.info("Loading ServiceTypeProviders");
        
        loadDefaults();

        for (ServiceTypeProvider provider : providers) {
            logger.fine("Loading ServiceTypeProvider: " + provider.getClass());
            ServiceTypeSetupContextImpl context = new ServiceTypeSetupContextImpl(provider.getClass());
            provider.setUp(context);
        }
        
        logResult();
    }

    private void loadDefaults() {
        logger.fine("Loading Default ServiceTypes");

        ServiceTypeSetupContextImpl context = new ServiceTypeSetupContextImpl(ServiceType.class);
        
        for (ServiceType type : ServiceType.DEFAULT_VALUES) {
            context.addServiceType(type);
        }
        
        for (AnnotationKey key : AnnotationKey.DEFAULT_VALUES) {
            context.addAnnotationKey(key);
        }
    }
    
    private void logResult() {
        logger.info("Finished loading ServiceTypeProviders");
        logger.info("ServiceType:");
        
        List<Pair<ServiceType>> serviceTypes = new ArrayList<Pair<ServiceType>>(serviceTypeChecker.serviceTypeCodeMap.values());
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
        
        
        logger.info("AnnotationKeys:");
        
        List<Pair<AnnotationKey>> annotationKeys = new ArrayList<Pair<AnnotationKey>>(annotationKeyChecker.annotationKeyCodeMap.values());
        Collections.sort(annotationKeys, new Comparator<Pair<AnnotationKey>>() {
            @Override
            public int compare(Pair<AnnotationKey> o1, Pair<AnnotationKey> o2) {
                int code1 = o1.value.getCode();
                int code2 = o2.value.getCode();
                
                return code1 > code2 ? 1 : (code1 < code2 ? -1 : 0);
            }
        });
        
        for (Pair<AnnotationKey> annotaionKey : annotationKeys) {
            logger.info(annotationKeyPairToString(annotaionKey));
        }
    }
    
    
    
    public List<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public List<AnnotationKey> getAnnotationKeys() {
        return annotationKeys;
    }


    private class ServiceTypeSetupContextImpl implements ServiceTypeSetupContext {
        private final Class<?> provider;
        
        public ServiceTypeSetupContextImpl(Class<?> provider) {
            this.provider = provider;
        }

        @Override
        public void addServiceType(ServiceType... serviceTypes) {
            for (ServiceType type : serviceTypes) {
                serviceTypeChecker.check(type, provider);
                ServiceTypeProviderLoader.this.serviceTypes.add(type);
            }
        }

        @Override
        public void addAnnotationKey(AnnotationKey... annotationKeys) {
            for (AnnotationKey key : annotationKeys) {
                annotationKeyChecker.check(key, provider);
                ServiceTypeProviderLoader.this.annotationKeys.add(key);
            }
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
    
    private static class ServiceTypeChecker { 
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
        
    }
    
    private static class AnnotationKeyChecker {
        private final Map<Integer, Pair<AnnotationKey>> annotationKeyCodeMap = new HashMap<Integer, Pair<AnnotationKey>>();

        private void check(AnnotationKey key, Class<?> providerClass) {
            Pair<AnnotationKey> pair = new Pair<AnnotationKey>(key, providerClass);
            Pair<AnnotationKey> prev = annotationKeyCodeMap.put(key.getCode(), pair);
    
            if (prev != null) {
                // TODO change exception type
                throw new RuntimeException("AnnotationKey code of " + annotationKeyPairToString(pair) + " is duplicated with " + annotationKeyPairToString(prev));
            }
        }
    }
    
    static void checkServiceTypes(List<ServiceType> serviceTypes) {
        ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker();
        
        for (ServiceType type : serviceTypes) {
            serviceTypeChecker.check(type, ServiceType.class);
        }
    }
    
    static void checkAnnotationKeys(List<AnnotationKey> annotationKeys) {
        AnnotationKeyChecker annotationKeyChecker = new AnnotationKeyChecker();
        
        for (AnnotationKey key : annotationKeys) {
            annotationKeyChecker.check(key, AnnotationKey.class);
        }
    }
    
    public static void initializeServiceType(ClassLoader classLoader) {
        ServiceTypeProviderLoader loader = new ServiceTypeProviderLoader();
        loader.load(classLoader);
        
        ServiceType.initialize(loader.getServiceTypes());
        AnnotationKey.initialize(loader.getAnnotationKeys());
    }
    
    public static void initializeServiceType(URL[] urls) {
        ServiceTypeProviderLoader loader = new ServiceTypeProviderLoader();
        loader.load(urls);
        
        ServiceType.initialize(loader.getServiceTypes());
        AnnotationKey.initialize(loader.getAnnotationKeys());
    }
    
    public static void initializeServiceType(List<ServiceTypeProvider> providers) {
        ServiceTypeProviderLoader loader = new ServiceTypeProviderLoader();
        loader.load(providers);
        
        ServiceType.initialize(loader.getServiceTypes());
        AnnotationKey.initialize(loader.getAnnotationKeys());
    }
}
