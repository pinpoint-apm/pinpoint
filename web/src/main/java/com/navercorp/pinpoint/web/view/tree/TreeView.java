package com.navercorp.pinpoint.web.view.tree;

import java.util.Iterator;

/**
 * @author emeroad
 */
public interface TreeView<C> {
    Iterator<TreeNode<C>> nodes();
}
