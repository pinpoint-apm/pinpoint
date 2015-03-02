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

import com.navercorp.pinpoint.common.plugin.*;
import com.navercorp.pinpoint.common.plugin.TypeProvider;

/**
 * @author Jongho Moon
 *
 */
public class TypeProviderLoader {
    private final Logger logger = Logger.getLogger(getClass().getName());
    
    private final ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker();
    private final AnnotationKeyChecker annotationKeyChecker = new AnnotationKeyChecker();
    
    private final List<Type> types = new ArrayList<Type>();
    private final List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>();
    private final boolean defaultServiceTypeLoad;

    public TypeProviderLoader() {
        this(true);
    }

    public TypeProviderLoader(boolean defaultServiceTypeLoad) {
        this.defaultServiceTypeLoad = defaultServiceTypeLoad;
    }

    public void load(URL[] urls) {
        List<TypeProvider> providers = PluginLoader.load(TypeProvider.class, urls);
        load(providers);
    }
    
    public void load(ClassLoader loader) {
        List<TypeProvider> providers = PluginLoader.load(TypeProvider.class, loader);
        load(providers);
    }
    
    void load(List<TypeProvider> providers) {
        logger.info("Loading ServiceTypeProviders");

        if (defaultServiceTypeLoad) {
            loadDefaults();
        }

        for (TypeProvider provider : providers) {
            logger.fine("Loading ServiceTypeProvider: " + provider.getClass());
            TypeSetupContextImpl context = new TypeSetupContextImpl(provider.getClass());
            provider.setUp(context);
        }
        
        logResult();
    }

    private void loadDefaults() {
        logger.fine("Loading Default ServiceTypes");

        TypeSetupContextImpl context = new TypeSetupContextImpl(ServiceType.class);

        for (ServiceType type : ServiceType.DEFAULT_VALUES) {
            context.addType(type);
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
    
    
    
    public List<Type> getTypes() {
        return types;
    }

    public List<AnnotationKey> getAnnotationKeys() {
        return annotationKeys;
    }


    private class TypeSetupContextImpl implements TypeSetupContext {
        private final Class<?> provider;
        
        public TypeSetupContextImpl(Class<?> provider) {
            this.provider = provider;
        }

        @Override
        public void addType(ServiceType serviceType) {
            Type type = new DefaultType(serviceType);
            addType(type);
        }

        @Override
        public void addType(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
            Type type = new DefaultType(serviceType, annotationKeyMatcher);
            addType(type);
        }


        public void addType(Type type) {
            if (type == null) {
                throw new NullPointerException("type must not be null");
            }
            serviceTypeChecker.check(type.getServiceType(), provider);
            TypeProviderLoader.this.types.add(type);
        }

        @Override
        public void addAnnotationKey(AnnotationKey annotationKey) {
            annotationKeyChecker.check(annotationKey, provider);
            TypeProviderLoader.this.annotationKeys.add(annotationKey);
        }
    }

    private void addType(Type type) {
        this.types.add(type);
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
        TypeProviderLoader loader = new TypeProviderLoader();
        loader.load(classLoader);
        initializeServiceType(loader);
    }

    public static void initializeServiceType(TypeProviderLoader loader) {
        if (loader == null) {
            throw new NullPointerException("loader must not be null");
        }
        List<Type> types = loader.getTypes();
        List<ServiceType> serviceTypes = getServiceTypeList(types);
        ServiceType.initialize(serviceTypes);
        AnnotationKey.initialize(loader.getAnnotationKeys());
    }
    
    public static void initializeServiceType(URL[] urls) {
        TypeProviderLoader loader = new TypeProviderLoader();
        loader.load(urls);

        List<Type> types = loader.getTypes();
        List<ServiceType> serviceTypes = getServiceTypeList(types);
        ServiceType.initialize(serviceTypes);
        AnnotationKey.initialize(loader.getAnnotationKeys());
    }
    
    public static void initializeServiceType(List<TypeProvider> providers) {
        TypeProviderLoader loader = new TypeProviderLoader();
        loader.load(providers);

        List<Type> types = loader.getTypes();
        List<ServiceType> serviceTypes = getServiceTypeList(types);
        ServiceType.initialize(serviceTypes);
        AnnotationKey.initialize(loader.getAnnotationKeys());
    }

    public static List<ServiceType> getServiceTypeList(List<Type> typeList) {
        if (typeList == null) {
            return Collections.emptyList();
        }
        List<ServiceType> serviceTypeList= new ArrayList<ServiceType>(typeList.size());
        for (Type type : typeList) {
            serviceTypeList.add(type.getServiceType());
        }
        return serviceTypeList;
    }
}
