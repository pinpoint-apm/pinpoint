package com.navercorp.pinpoint.metric.common.model.validation;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Objects;

public class SimpleErrorMessage {

    private final BindingResult bindingResult;

    public SimpleErrorMessage(BindingResult bindingResult) {
        this.bindingResult = Objects.requireNonNull(bindingResult, "bindingResult");
    }

    public String getError() {
        if (!bindingResult.hasErrors()) {
            return "NoError";
        }
        ObjectError sample = CollectionUtils.firstElement(bindingResult.getAllErrors());
        return Objects.toString(sample);
    }

    @Override
    public String toString() {
        return "SampleErrorMessage{" +
                 getError() +
                '}';
    }
}
