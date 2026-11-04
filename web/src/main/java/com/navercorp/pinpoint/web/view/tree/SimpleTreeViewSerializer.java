package com.navercorp.pinpoint.web.view.tree;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

import java.util.Iterator;

/**
 * @author emeroad
 */
public class SimpleTreeViewSerializer<I, C> extends ValueSerializer<SimpleTreeView<I, C>> {

    @Override
    public void serialize(SimpleTreeView<I, C> treeView, JsonGenerator jgen, SerializationContext provider) {
        jgen.writeStartObject();
        Iterator<TreeNode<C>> nodes = treeView.nodes();
        while (nodes.hasNext()) {
            TreeNode<C> treeNode = nodes.next();
            jgen.writeName(treeNode.getValue());
            jgen.writePOJO(treeNode.getChildren());
        }
        jgen.writeEndObject();
    }
}
