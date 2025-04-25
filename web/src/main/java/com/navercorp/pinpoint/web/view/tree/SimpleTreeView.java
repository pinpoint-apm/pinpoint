package com.navercorp.pinpoint.web.view.tree;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author emeroad
 */
@JsonSerialize(using = SimpleTreeViewSerializer.class)
public class SimpleTreeView<I, C> implements TreeView<TreeNode<C>> {

    private final List<I> nodeList;
    private final Function<I, String> valueMapper;
    private final Function<I, List<C>> childMapper;

    public SimpleTreeView(List<I> nodeList,
                           Function<I, String> valueMapper,
                           Function<I, List<C>> childMapper) {
        this.nodeList = Objects.requireNonNull(nodeList, "nodeList");
        this.valueMapper = Objects.requireNonNull(valueMapper, "valueMapper");
        this.childMapper = Objects.requireNonNull(childMapper, "childMapper");
    }

    @Override
    public Iterator<TreeNode<C>> nodes() {
        return Iterators.transform(nodeList.iterator(), this::toTreeNode);
    }

    private TreeNode<C> toTreeNode(I item) {
        return new TreeNodeView<>(item, valueMapper, childMapper);
    }


    @Override
    public String toString() {
        return "TreeView{" +
                "itemList=" + nodeList +
                '}';
    }

}
