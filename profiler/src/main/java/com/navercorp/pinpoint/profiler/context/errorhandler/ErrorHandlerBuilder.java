package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandlerBuilder {

    private final List<Descriptor> descriptorList;

    public ErrorHandlerBuilder(List<Descriptor> descriptorList) {
        this.descriptorList = Assert.requireNonNull(descriptorList, "descriptorList");
    }

    public ErrorHandler build() {
        List<ErrorHandler> errorHandlerList = new ArrayList<ErrorHandler>();
        for (Descriptor desc : descriptorList) {
            List<String> classNames = desc.getClassNames();
            ThrowableMatcher throwableMatcher = new ClassNameThrowableMatcher(classNames);
            MessageMatcher messageMatcher = newMessageMatcher(desc.getContainsExceptionMessages());

            ErrorHandler errorHandler = new DefaultErrorHandler(desc.getHandlerId(), throwableMatcher, messageMatcher);
            if (desc.isNestedSearch()) {
                errorHandler = new NestedErrorHandler(errorHandler);
            }
            errorHandlerList.add(errorHandler);
        }

        return new MultipleErrorHandler(errorHandlerList);
    }

    private MessageMatcher newMessageMatcher(List<String> containsExceptionMessages) {
        if (CollectionUtils.isEmpty(containsExceptionMessages)) {
            return EmptyMessageMatcher.EMPTY_MESSAGE_MATCHER;
        }
        return new ContainsMessageMatcher(containsExceptionMessages);
    }
}
