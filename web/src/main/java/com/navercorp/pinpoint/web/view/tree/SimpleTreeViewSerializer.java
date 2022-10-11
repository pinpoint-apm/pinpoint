package com.navercorp.pinpoint.web.view.tree;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author emeroad
 */
public class SimpleTreeViewSerializer<I, C> extends JsonSerializer<SimpleTreeView<I, C>> {

    @Override
    public void serialize(SimpleTreeView<I, C> treeView, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        Iterator<TreeNode<C>> nodes = treeView.nodes();
        while (nodes.hasNext()) {
            TreeNode<C> treeNode = nodes.next();
            jgen.writeFieldName(treeNode.getValue());
            jgen.writeObject(treeNode.getChildren());
        }
        jgen.writeEndObject();
    }
}
