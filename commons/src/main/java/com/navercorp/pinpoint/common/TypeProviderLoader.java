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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.navercorp.pinpoint.common.plugin.*;
import com.navercorp.pinpoint.common.plugin.TypeProvider;

/**
 * @author Jongho Moon
 *
 */
public class TypeProviderLoader {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final List<Type> types = new ArrayList<Type>();
    private final ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker();

    private final List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>();
    private final AnnotationKeyChecker annotationKeyChecker = new AnnotationKeyChecker();


    public TypeProviderLoader() {
    }


    public void load(URL[] urls) {
        if (urls == null) {
            throw new NullPointerException("urls must not be null");
        }

        List<TypeProvider> providers = PluginLoader.load(TypeProvider.class, urls);
        load(providers);
    }
    
    public void load(ClassLoader loader) {
        if (loader == null) {
            throw new NullPointerException("loader must not be null");
        }

        List<TypeProvider> providers = PluginLoader.load(TypeProvider.class, loader);
        load(providers);
    }
    
    public void load(List<TypeProvider> providers) {
        if (providers == null) {
            throw new NullPointerException("providers must not be null");
        }

        logger.info("Loading TypeProviders");

        final List<TypeSetupContextImpl> setupContextList = new ArrayList<TypeSetupContextImpl>();

        for (TypeProvider provider : providers) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Loading TypeProvider: " + provider.getClass().getName() + " name:" + provider.toString());
            }

            TypeSetupContextImpl context = new TypeSetupContextImpl(provider.getClass());
            provider.setup(context);


            setupContextList.add(context);
        }

        buildType(setupContextList);
        buildAnnotationKey(setupContextList);
    }

    public void buildType(List<TypeSetupContextImpl> setupContextList) {

        for (TypeSetupContextImpl typeSetupContext : setupContextList) {
            for (Type type : typeSetupContext.getTypes()) {
                this.serviceTypeChecker.check(type.getServiceType(), typeSetupContext.getProvider());
                types.add(type);
            }
        }
        this.serviceTypeChecker.logResult();
    }

    public void buildAnnotationKey(List<TypeSetupContextImpl> setupContextList) {

        for (TypeSetupContextImpl typeSetupContext : setupContextList) {
            for (AnnotationKey annotationKey : typeSetupContext.getAnnotationKeys()) {
                this.annotationKeyChecker.check(annotationKey, typeSetupContext.getProvider());
                annotationKeys.add(annotationKey);
            }
        }
        this.annotationKeyChecker.logResult();
    }


    public List<Type> getTypes() {
        return types;
    }

    public List<AnnotationKey> getAnnotationKeys() {
        return annotationKeys;
    }


    private class TypeSetupContextImpl implements TypeSetupContext {
        private final Class<?> provider;
        private final List<Type> types = new ArrayList<Type>();
        private final List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>();

        private final ServiceTypeChecker contextServiceTypeChecker = new ServiceTypeChecker();
        private final AnnotationKeyChecker contextAnnotationKeyChecker = new AnnotationKeyChecker();
        
        public TypeSetupContextImpl(Class<?> provider) {
            this.provider = provider;
        }

        private Class<?> getProvider() {
            return provider;
        }

        @Override
        public void addType(ServiceType serviceType) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType must not be null");
            }
            Type type = new DefaultType(serviceType);
            addType0(type);
        }

        @Override
        public void addType(ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
            if (serviceType == null) {
                throw new NullPointerException("serviceType must not be null");
            }
            if (annotationKeyMatcher == null) {
                throw new NullPointerException("annotationKeyMatcher must not be null");
            }
            Type type = new DefaultType(serviceType, annotationKeyMatcher);
            addType0(type);
        }

        private void addType0(Type type) {
            if (type == null) {
                throw new NullPointerException("type must not be null");
            }
            // local check
            contextServiceTypeChecker.check(type.getServiceType(), provider);
            this.types.add(type);
        }

        private List<Type> getTypes() {
            return types;
        }

        @Override
        public void addAnnotationKey(AnnotationKey annotationKey) {
            if (annotationKey == null) {
                throw new NullPointerException("annotationKey must not be null");
            }
            // local check
            contextAnnotationKeyChecker.check(annotationKey, provider);
            this.annotationKeys.add(annotationKey);
        }

        private List<AnnotationKey> getAnnotationKeys() {
            return annotationKeys;
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

            for (Pair<AnnotationKey> annotaionKey : annotationKeys) {
                logger.info(annotationKeyPairToString(annotaionKey));
            }
        }
    }


}
