package com.navercorp.pinpoint.profiler.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PluginPackageClassRequirementFilter implements ClassNameFilter {

    private final String[] packages;
    private final String[] requirements;
    private final Logger logger = LogManager.getLogger(this.getClass());

    public PluginPackageClassRequirementFilter(List<String> packageRequirementList) {
        Objects.requireNonNull(packageRequirementList, "packageRequirementList");

        final List<String> packageList = new ArrayList<>();
        final List<String> requirementList = new ArrayList<>();
        parseRequirementList(packageRequirementList, packageList, requirementList);

        packages = packageList.toArray(new String[0]);
        requirements = requirementList.toArray(new String[0]);
    }

    @Override
    public boolean accept(String className, ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader"); 
        
        for (int i = 0; i < packages.length; i++) {
            if (className.startsWith(packages[i])) {
                if (!isLoadedClass(requirements[i], classLoader)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("reject class:{}, packageName:{}, requirement:{}, classloader:{}", className, packages[i], requirements[i], classLoader);
                    }
                    return REJECT;
                }
            }
        }
        return ACCEPT;
    }

    private boolean isLoadedClass(String classname, ClassLoader cl) {
        try {
            Class.forName(classname, false, cl);
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    private void parseRequirementList(List<String> packageRequirementList, List<String> packageList, List<String> requirementList) {
        for (String packageWithRequirement : packageRequirementList) {
            String[] split = packageWithRequirement.split(":");
            if (split.length == 2) {
                packageList.add(split[0]);
                requirementList.add(split[1]);
            }
        }
    }

    @Override
    public String toString() {
        return "PluginPackageRequirementFilter{" +
                "packages=" + Arrays.toString(packages) +
                ", requirements=" + Arrays.toString(requirements) +
                '}';
    }
}
