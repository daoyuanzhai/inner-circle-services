package com.innercircle.services.model;

import java.util.List;

public class InnerCircleTestOuter {
    private String field1;
    private String field2;
    private List<InnerCircleTestInner> innerList;

    public void setField1(final String field1) {
        this.field1 = field1;
    }

    public String getField1() {
        return this.field1;
    }

    public void setField2(final String field2) {
        this.field2 = field2;
    }

    public String getField2() {
        return this.field2;
    }

    public void setInnerList(final List<InnerCircleTestInner> innerList) {
        this.innerList = innerList;
    }

    public List<InnerCircleTestInner> getInnerList() {
        return this.innerList;
    }
}
