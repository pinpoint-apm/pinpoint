package com.profiler.modifier;

import com.profiler.modifier.tomcat.EntryPointStandardHostValveModifier;
import com.profiler.modifier.tomcat.TomcatConnectorModifier;
import com.profiler.modifier.tomcat.TomcatStandardServiceModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultModifierRegistry implements ModifierRegistry {
    // TODO 혹시 동시성을 고려 해야 되는지 검토.
    private Map<String, Modifier> registry = new HashMap<String, Modifier>();


    private List<String> packageIncludeFilters = new ArrayList<String>();

    @Override
    public Modifier findModifier(String className) {
        if(!findPackage(className)) {
            return null;
        }
        return registry.get(className);
    }

    private boolean findPackage(String className) {
        for(String filter : packageIncludeFilters) {
            if(filter.equals(className)) {
                return true;
            }
        }
        return false;
    }

    public void addTomcatModifier() {
        packageIncludeFilters.add("org/apache/catalina");

        Map<String, Modifier> registry = this.registry;
        Modifier entryPointStandardHostValveModifier = new EntryPointStandardHostValveModifier();
        registry.put("org/apache/catalina/core/StandardHostValve", entryPointStandardHostValveModifier);

        Modifier tomcatStandardServiceModifier = new TomcatStandardServiceModifier();
        registry.put("org/apache/catalina/core/StandardService", tomcatStandardServiceModifier);

        Modifier tomcatConnectorModifier = new TomcatConnectorModifier();
        registry.put("org/apache/catalina/connector/Connector", tomcatConnectorModifier);
    }

    public void addJdbcModifier() {


    }
}
