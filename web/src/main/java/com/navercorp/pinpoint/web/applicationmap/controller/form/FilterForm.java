package com.navercorp.pinpoint.web.applicationmap.controller.form;

import com.navercorp.pinpoint.web.validation.NullOrNotBlank;

public class FilterForm {
    @NullOrNotBlank
    private String filter;
    @NullOrNotBlank
    private String hint;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filterText) {
        this.filter = filterText;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String filterHint) {
        this.hint = filterHint;
    }
}
