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

        assertEquals(ExceptionTraceValueView.EMPTY_STRING, view.getFieldName());

        GroupedFieldName nullStringFieldName = new GroupedFieldName();
        emptyFieldName.setErrorMessage("null");
        view.setGroupedFieldName(emptyFieldName);

        assertEquals(ExceptionTraceValueView.EMPTY_STRING, view.getFieldName());

        GroupedFieldName nullFieldName = new GroupedFieldName();
        view.setGroupedFieldName(nullFieldName);

        assertEquals(ExceptionTraceValueView.TOTAL_FIELDNAME, view.getFieldName());


        view.setGroupedFieldName(null);

        assertEquals(ExceptionTraceValueView.TOTAL_FIELDNAME, view.getFieldName());
    }
}