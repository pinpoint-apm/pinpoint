/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import java.util.List;

import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.web.applicationmap.link.CreateType;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author emeroad
 */
public class LinkListTest {
    private static final ServiceType TOMCAT = ServiceTypeFactory.of(1010, "TOMCAT", RECORD_STATISTICS);
    private static final ServiceType BLOC = ServiceTypeFactory.of(1011, "BLOC", RECORD_STATISTICS);
      
    
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

        // don't copy in case of duplicated node
        original.addLinkList(copy);
        Assert.assertEquals(original.size(), 1);

    }

    private Link createTomcatToTomcatLink() {
        LinkList linkList = new LinkList();
        Node from = new Node(new Application("from", TOMCAT));
        Node to = new Node(new Application("to", TOMCAT));
        Link link = new Link(CreateType.Source, from, to, new Range(0, 0));
        return link;
    }

    private Link createTomcatToBlocLink() {
        LinkList linkList = new LinkList();
        Node from = new Node(new Application("from", TOMCAT));
        Node to = new Node(new Application("to", BLOC));
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

        // find all links requesting "to"
        Application toBloc = new Application("to", BLOC);
        List<Link> findToLink = list.findToLink(toBloc);
        Assert.assertEquals(findToLink.size(), 1);

        for (Link link : findToLink) {
            Application to = link.getTo().getApplication();
            Assert.assertTrue(toBloc + " " + to, toBloc.equals(to));
        }

        List<Link> unknown = list.findToLink(new Application("unknown", BLOC));
        Assert.assertEquals(unknown.size(), 0);
    }

    @Test
    public void testFindFromLink() throws Exception {
        Link tomcatToBlocLink = createTomcatToBlocLink();
        LinkList list = new LinkList();
        list.addLink(tomcatToBlocLink);
        Link tomcatToTomcatLink = createTomcatToTomcatLink();
        list.addLink(tomcatToTomcatLink);

        // find all links for "from" to request
        Application tomcat = new Application("from", TOMCAT);
        List<Link> findFromLink = list.findFromLink(tomcat);
        Assert.assertEquals(findFromLink.size(), 2);
        for (Link link : findFromLink) {
            Application linkFrom = link.getFrom().getApplication();
            Assert.assertTrue(linkFrom.equals(tomcat));
        }

        List<Link> unknown = list.findFromLink(new Application("unknown", TOMCAT));
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
