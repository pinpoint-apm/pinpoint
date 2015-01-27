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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.navercorp.pinpoint.common.plugin.ServiceTypeProvider;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class ServiceTypeInitializer {
    private static final Logger logger = Logger.getLogger(ServiceTypeInitializer.class.getName());
    
    public static void initialize() {
        initialize(Collections.<ServiceTypeProvider>emptyList());
    }
    
    public static void initialize(List<ServiceTypeProvider> providers) {
        List<ServiceType> serviceTypes = new ArrayList<ServiceType>();
        List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>();
        
        ServiceTypeChecker serviceTypeChecker = new ServiceTypeChecker();
        AnnotationKeyChecker annotationKeyChecker = new AnnotationKeyChecker();
        
        
        for (ServiceType type : ServiceType.DEFAULT_VALUES) {
            serviceTypeChecker.check(type, ServiceType.class);
            serviceTypes.add(type);
            
            logger.info("Adding default service type: " + type.getName() + "[" + type.getCode() + "]");
        }
        
        for (AnnotationKey key : AnnotationKey.DEFAULT_VALUES) {
            annotationKeyChecker.check(key, AnnotationKey.class);
            annotationKeys.add(key);
            
            logger.info("Adding default annotaion key: " + key.getValue() + "[" + key.getCode() + "]");
        }



        for (ServiceTypeProvider provider : providers) {
            Class<?> providerClass = provider.getClass();
            
            for (ServiceType type : provider.getServiceTypes()) {
                serviceTypeChecker.check(type, providerClass);
                serviceTypes.add(type);
                
                logger.info("Adding service type from " + providerClass.getName() + ": " + type.getName() + "[" + type.getCode() + "]");
            }

            for (AnnotationKey key : provider.getAnnotationKeys()) {
                annotationKeyChecker.check(key, providerClass);
                annotationKeys.add(key);
                
                logger.info("Adding annotation key from " + providerClass.getName() + ": " + key.getValue() + "[" + key.getCode() + "]");
            }
        }

        initializeServiceType(serviceTypes);
        initializeAnnotationKey(annotationKeys);
    }
    
    private static IntHashMap<AnnotationKey> initializeAnnotationKeyCodeLookupTable(List<AnnotationKey> annotationKeys) {
        IntHashMap<AnnotationKey> table = new IntHashMap<AnnotationKey>();
        
        for (AnnotationKey serviceType : annotationKeys) {
            table.put(serviceType.getCode(), serviceType);
        }
        
        return table;
    }

    static void initializeAnnotationKey(List<AnnotationKey> annotationKeys) {
        List<AnnotationKey> unmodifiableAnnotaionKeys = Collections.unmodifiableList(annotationKeys);
        IntHashMap<AnnotationKey> codeTable = initializeAnnotationKeyCodeLookupTable(annotationKeys);
        
        AnnotationKey.initialize(unmodifiableAnnotaionKeys, codeTable);
    }

    
    private static Map<String, List<ServiceType>> initializeServiceTypeStatisticsLookupTable(List<ServiceType> serviceTypes) {
        final Map<String, List<ServiceType>> table = new HashMap<String, List<ServiceType>>();

        for (ServiceType serviceType : serviceTypes) {
            if (serviceType.isRecordStatistics()) {
                List<ServiceType> serviceTypeList = table.get(serviceType.getDesc());
                if (serviceTypeList == null) {
                    serviceTypeList = new ArrayList<ServiceType>();
                    table.put(serviceType.getDesc(), serviceTypeList);
                }
                serviceTypeList.add(serviceType);
            }
        }

        // value of this table will be exposed. so make them unmodifiable.
        final Map<String, List<ServiceType>> unmodifiable = new HashMap<String, List<ServiceType>>(table.size());

        for (Map.Entry<String, List<ServiceType>> entry : table.entrySet()) {
            List<ServiceType> newValue = Collections.unmodifiableList(entry.getValue());
            unmodifiable.put(entry.getKey(), newValue);
        }

        return unmodifiable;
    }
    
    private static void initializeServiceType(List<ServiceType> serviceTypes) {
        List<ServiceType> unmodifiableServiceTypes = Collections.unmodifiableList(serviceTypes);
        IntHashMap<ServiceType> codeTable = initializeServiceTypeCodeLookupTable(serviceTypes);
        Map<String, List<ServiceType>> statisticsTable = initializeServiceTypeStatisticsLookupTable(serviceTypes);
        
        ServiceType.initialize(unmodifiableServiceTypes, codeTable, statisticsTable);

    }

    private static IntHashMap<ServiceType> initializeServiceTypeCodeLookupTable(List<ServiceType> serviceTypes) {
        IntHashMap<ServiceType> table = new IntHashMap<ServiceType>(256);

        for (ServiceType serviceType : serviceTypes) {
            table.put(serviceType.getCode(), serviceType);
        }

        return table;
    }


    private static class ServiceTypeChecker { 
        private final Map<String, Class<?>> serviceTypeNameMap = new HashMap<String, Class<?>>();
        private final Map<Short, Class<?>> serviceTypeCodeMap = new HashMap<Short, Class<?>>();

        private void check(ServiceType type, Class<?> providerClass) {
            Class<?> prevProviderClass = serviceTypeNameMap.put(type.getName(), providerClass);
    
            if (prevProviderClass != null) {
                // TODO change exception type
                throw new RuntimeException("Duplicated ServiceType name '" + type.getName() + "'. provided by " + providerClass.getName() + " and " + prevProviderClass.getName());
            }
    
            prevProviderClass = serviceTypeCodeMap.put(type.getCode(), providerClass);
    
            if (prevProviderClass != null) {
                // TODO change exception type
                throw new RuntimeException("Duplicated ServiceType code '" + type.getCode() + "'. provided by " + providerClass.getName() + " and " + prevProviderClass.getName());
            }
        }
    }
    
    private static class AnnotationKeyChecker {
        private final Map<Integer, Class<?>> annotationKeyCodeMap = new HashMap<Integer, Class<?>>();

        private void check(AnnotationKey key, Class<?> providerClass) {
            Class<?> prevProviderClass = annotationKeyCodeMap.put(key.getCode(), providerClass);
    
            if (prevProviderClass != null) {
                // TODO change exception type
                throw new RuntimeException("Duplicated AnnotationKey code '" + key.getCode() + "'. provided by " + providerClass.getName() + " and " + prevProviderClass.getName());
            }
        }
    }
}
