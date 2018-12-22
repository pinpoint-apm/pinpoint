package com.navercorp.pinpoint.profiler.transaction.springmvc;

import com.navercorp.pinpoint.bootstrap.context.transaction.IMappingRegistry;

import java.util.ArrayList;
import java.util.List;

public class SpringMVCMappingRegistryFactory {

    public static List<IMappingRegistry> createMappingRegistries(){
        List<IMappingRegistry> registries = new ArrayList<IMappingRegistry>();

        registries.add(new MethodHandlerRegistryMapping());
        registries.add(new UrlHandlerRegistryMapping());

        return registries;
    }
}
