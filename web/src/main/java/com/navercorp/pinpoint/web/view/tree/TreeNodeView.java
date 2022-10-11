package com.navercorp.pinpoint.web.view.tree;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author emeroad
 */
public class TreeNodeView<I, V> implements TreeNode<V> {
    private final I item;
    private final Function<I, String> valueMapper;
    private final Function<I, List<V>> childMapper;

    public TreeNodeView(I item, Function<I, String> valueMapper, Function<I, List<V>> childMapper) {
        this.item = Objects.requireNonNull(item, "item");
        this.valueMapper = Objects.requireNonNull(valueMapper, "valueMapper");
        this.childMapper = Objects.requireNonNull(childMapper, "childMapper");
    }

    @Override
    public String getValue() {
        return valueMapper.apply(item);
    }

    @Override
    public List<V> getChildren() {
        return childMapper.apply(item);
    }
}
