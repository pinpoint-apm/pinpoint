package com.navercorp.pinpoint.exceptiontrace.web.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class ExceptionTraceValueViewTest {

    @Test
    void testGetFieldName() {
        ExceptionTraceValueView view = new ExceptionTraceValueView();

        GroupedFieldName emptyFieldName = new GroupedFieldName();
        emptyFieldName.setErrorMessage("");
        view.setGroupedFieldName(emptyFieldName);

        assertEquals("", view.getFieldName());


        GroupedFieldName nullFieldName = new GroupedFieldName();
        view.setGroupedFieldName(nullFieldName);

        assertEquals("total", view.getFieldName());


        view.setGroupedFieldName(null);

        assertEquals("total", view.getFieldName());
    }
}