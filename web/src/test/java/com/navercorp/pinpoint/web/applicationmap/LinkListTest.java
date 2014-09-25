package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author emeroad
 */
public class LinkListTest {
    @Test
    public void testGetLinkList() throws Exception {
        LinkList linkList = new LinkList();
        Assert.assertEquals(linkList.getLinkList().size(), 0);
    }

    @Test
    public void addLinkList() throws Exception {
        Link tomcatToTomcatLink = createTomcatToTomcatLink();
        LinkList copy = new LinkList();
        copy.addLink(tomcatToTomcatLink);

        LinkList original = new LinkList();
        original.addLinkList(copy);
        Assert.assertEquals(original.size(), 1);

        // 중복 노드는 copy하면 안됨
        original.addLinkList(copy);
        Assert.assertEquals(original.size(), 1);

    }

    private Link createTomcatToTomcatLink() {
        LinkList linkList = new LinkList();
        Node from = new Node(new Application("from", ServiceType.TOMCAT));
        Node to = new Node(new Application("to", ServiceType.TOMCAT));
        Link link = new Link(CreateType.Source, from, to, new Range(0, 0));
        return link;
    }

    private Link createTomcatToBlocLink() {
        LinkList linkList = new LinkList();
        Node from = new Node(new Application("from", ServiceType.TOMCAT));
        Node to = new Node(new Application("to", ServiceType.BLOC));
        Link link = new Link(CreateType.Source, from, to, new Range(0, 0));
        return link;
    }

    @Test
    public void testFindToLink() throws Exception {
        Link tomcatToBlocLink = createTomcatToBlocLink();
        LinkList list = new LinkList();
        list.addLink(tomcatToBlocLink);
        Link tomcatToTomcatLink = createTomcatToTomcatLink();
        list.addLink(tomcatToTomcatLink);

        // to를 호출하는 모든 링크를 찾음.
        Application toBloc = new Application("to", ServiceType.BLOC);
        List<Link> findToLink = list.findToLink(toBloc);
        Assert.assertEquals(findToLink.size(), 1);

        for (Link link : findToLink) {
            Application to = link.getTo().getApplication();
            Assert.assertTrue(toBloc + " " + to, toBloc.equals(to));
        }

        List<Link> unknown = list.findToLink(new Application("unknown", ServiceType.BLOC));
        Assert.assertEquals(unknown.size(), 0);
    }

    @Test
    public void testFindFromLink() throws Exception {
        Link tomcatToBlocLink = createTomcatToBlocLink();
        LinkList list = new LinkList();
        list.addLink(tomcatToBlocLink);
        Link tomcatToTomcatLink = createTomcatToTomcatLink();
        list.addLink(tomcatToTomcatLink);

        // from에서 호출하는 링크를 모두 찾음.
        Application tomcat = new Application("from", ServiceType.TOMCAT);
        List<Link> findFromLink = list.findFromLink(tomcat);
        Assert.assertEquals(findFromLink.size(), 2);
        for (Link link : findFromLink) {
            Application linkFrom = link.getFrom().getApplication();
            Assert.assertTrue(linkFrom.equals(tomcat));
        }

        List<Link> unknown = list.findFromLink(new Application("unknown", ServiceType.TOMCAT));
        Assert.assertEquals(unknown.size(), 0);
    }

    @Test
    public void testContainsNode() throws Exception {
        Link tomcatToBlocLink = createTomcatToBlocLink();
        LinkList list = new LinkList();
        Assert.assertFalse(list.containsNode(tomcatToBlocLink));

        list.addLink(tomcatToBlocLink);

        Assert.assertTrue(list.containsNode(tomcatToBlocLink));
    }

    @Test
    public void testSize() throws Exception {
        LinkList list = new LinkList();
        Assert.assertEquals(list.size(), 0);

        list.addLink(createTomcatToTomcatLink());
        Assert.assertEquals(list.size(), 1);
    }
}
