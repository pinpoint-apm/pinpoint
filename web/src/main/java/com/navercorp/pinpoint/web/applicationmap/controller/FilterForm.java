package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.web.validation.NullOrNotBlank;

public class FilterForm {
    @NullOrNotBlank
    private String filterText;
    @NullOrNotBlank
    private String filterHint;

    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public String getFilterHint() {
        return filterHint;
    }

    public void setFilterHint(String filterHint) {
        this.filterHint = filterHint;
    }
}
