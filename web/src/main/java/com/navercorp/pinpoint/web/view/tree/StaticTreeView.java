package com.navercorp.pinpoint.web.view.tree;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StaticTreeView<C> implements TreeView<C> {

    private final List<C> nodeList;

    public StaticTreeView(List<C> nodeList) {
        this.nodeList = Objects.requireNonNull(nodeList, "nodeList");
    }

    @Override
    @JsonValue
    @JsonUnwrapped
    public Iterator<C> nodes() {
        return nodeList.stream().iterator();
    }

    @Override
    public String toString() {
        return "TreeView{" +
                "itemList=" + nodeList +
                '}';
    }

}
