package com.navercorp.pinpoint.web.view.tree;

import java.util.List;

/**
 * @author emeroad
 */
public interface TreeNode<V> {
    String getValue();

    List<V> getChildren();
}
