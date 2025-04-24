package com.navercorp.pinpoint.web.applicationmap.controller.form;

import jakarta.validation.constraints.Positive;

public class GroupForm {
    @Positive
    private int xGroupUnit;
    @Positive
    private int yGroupUnit;

    public int geYGroupUnit() {
        return yGroupUnit;
    }

    public void setYGroupUnit(int yGroupUnit) {
        this.yGroupUnit = yGroupUnit;
    }

    public int getXGroupUnit() {
        return xGroupUnit;
    }

    public void setXGroupUnit(int xGroupUnit) {
        this.xGroupUnit = xGroupUnit;
    }

    @Override
    public String toString() {
        return "GroupForm{" +
                "xGroupUnit=" + xGroupUnit +
                ", yGroupUnit=" + yGroupUnit +
                '}';
    }
}
