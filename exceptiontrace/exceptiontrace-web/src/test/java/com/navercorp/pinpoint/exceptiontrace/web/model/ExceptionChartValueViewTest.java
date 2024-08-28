package com.navercorp.pinpoint.exceptiontrace.web.model;

import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class ExceptionChartValueViewTest {

    @Test
    void testGetFieldName() {
        ExceptionChartValueView view = new ExceptionChartValueView();
        view.setRowNum(1);


        GroupedFieldName nullStringFieldName = new GroupedFieldName();
        nullStringFieldName.setErrorMessage("null");
        view.setGroupedFieldName(nullStringFieldName);
        assertEquals("1) null", view.getFieldName());


        GroupedFieldName nullFieldName = new GroupedFieldName();
        view.setGroupedFieldName(nullFieldName);
        assertEquals(ExceptionChartValueView.TOTAL_FIELDNAME, view.getFieldName());


        view.setGroupedFieldName(null);
        assertEquals(ExceptionChartValueView.TOTAL_FIELDNAME, view.getFieldName());
    }
}