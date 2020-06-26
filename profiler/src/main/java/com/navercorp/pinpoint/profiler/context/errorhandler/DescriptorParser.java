package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DescriptorParser {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, String> property;

    public DescriptorParser(Map<String, String> property) {
        this.property = Assert.requireNonNull(property, "property");
    }

    public List<Descriptor> parse() {

        final Set<String> handlerIdSet = parseHandlerId();

        List<Descriptor> descriptorList = new ArrayList<Descriptor>();
        for (String handlerId : handlerIdSet) {
            final List<String> classNameList = getClassName(handlerId);
            if (classNameList.isEmpty()) {
                logger.info("{} is empty", OptionKey.getClassName(handlerId));
                continue;
            }

            final List<String> exceptionMessage = getExceptionMessage(handlerId);
            final boolean nestedValue = getNestedValue(handlerId);

            Descriptor descriptor = new Descriptor(handlerId, classNameList, exceptionMessage, nestedValue);
            descriptorList.add(descriptor);
        }
        return descriptorList;
    }

    private List<String> getClassName(String handlerId) {
        String classNameKey = OptionKey.getClassName(handlerId);
        String classNameValue = this.property.get(classNameKey);
        return StringUtils.tokenizeToStringList(classNameValue, ",");
    }

    private List<String> getExceptionMessage(String handlerId) {
        String exceptionMessageKey = OptionKey.getExceptionMessageContains(handlerId);
        String exceptionMessageValue = property.get(exceptionMessageKey);
        return StringUtils.tokenizeToStringList(exceptionMessageValue, ",");
    }

    private boolean getNestedValue(String handlerId) {
        String nestedKey = OptionKey.getNested(handlerId);
        String nestedValue = property.get(nestedKey);
        if (nestedKey == null) {
            return true;
        }
        return Boolean.parseBoolean(nestedValue);
    }

    private String getValue(Map<String, String> property, String classNameKey) {
        return property.get(classNameKey);
    }

    private Set<String> parseHandlerId() {
        Set<String> handlerIdSet = new HashSet<String>();

        for (String key : property.keySet()) {
            String handlerId = OptionKey.parseHandlerId(key);
            if (handlerId != null) {
                handlerIdSet.add(handlerId);
            }
        }
        return handlerIdSet;
    }
}
