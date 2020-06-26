package com.navercorp.pinpoint.profiler.context.errorhandler;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandlerBuilder {

    private final List<Descriptor> descriptorList;

    // reuse
    private final AlwaysMessageMatcher ALWAYS = new AlwaysMessageMatcher();

    public ErrorHandlerBuilder(List<Descriptor> descriptorList) {
        this.descriptorList = Assert.requireNonNull(descriptorList, "descriptorList");
    }

    public IgnoreErrorHandler build() {
        List<IgnoreErrorHandler> errorHandlerList = new ArrayList<IgnoreErrorHandler>();
        for (Descriptor desc : descriptorList) {

            ThrowableMatcher throwableMatcher = newThrowableMatcher(desc);
            MessageMatcher messageMatcher = newMessageMatcher(desc.getContainsExceptionMessages());

            IgnoreErrorHandler errorHandler = new DefaultIgnoreErrorHandler(desc.getHandlerId(), throwableMatcher, messageMatcher);
            if (desc.isNestedSearch()) {
                errorHandler = new NestedErrorHandler(errorHandler);
            }
            errorHandlerList.add(errorHandler);
        }

        if (errorHandlerList.isEmpty()) {
            return new BypassErrorHandler();
        }
        return new MultipleErrorHandler(errorHandlerList.toArray(new IgnoreErrorHandler[0]));
    }

    private ThrowableMatcher newThrowableMatcher(Descriptor descriptor) {
        List<String> classNames = descriptor.getClassNames();
        ThrowableMatcher throwableMatcher = new ClassNameThrowableMatcher(classNames.toArray(new String[0]));
        if (descriptor.isParentSearch()) {
           throwableMatcher = new ParentClassThrowableMatcher(throwableMatcher);
        }
        return throwableMatcher;
    }

    private MessageMatcher newMessageMatcher(List<String> containsExceptionMessages) {
        if (CollectionUtils.isEmpty(containsExceptionMessages)) {
            return ALWAYS;
        }
        final String[] patterns = containsExceptionMessages.toArray(new String[0]);
        return new ContainsMessageMatcher(patterns);
    }
}
