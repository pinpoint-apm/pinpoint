package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.TestTraceUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static com.navercorp.pinpoint.web.TestTraceUtils.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author emeroad
 */
public class LinkFilterTest {

    private static final int RPC_ANNOTATION_CODE = -1;
    private static final String RPC_ANNOTATION_NAME = "rpc.url";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ServiceTypeRegistryService serviceTypeRegistryService = TestTraceUtils.mockServiceTypeRegistryService();
    private final AnnotationKeyRegistryService annotationKeyRegistryService = mockAnnotationKeyRegistryService();

    private AnnotationKeyRegistryService mockAnnotationKeyRegistryService() {
        final AnnotationKey rpcUrlAnnotationKey = AnnotationKeyFactory.of(RPC_ANNOTATION_CODE, RPC_ANNOTATION_NAME);
        return new AnnotationKeyRegistryService() {
            @Override
            public AnnotationKey findAnnotationKey(int annotationCode) {
                return rpcUrlAnnotationKey;
            }

            @Override
            public AnnotationKey findAnnotationKeyByName(String keyName) {
                return rpcUrlAnnotationKey;
            }

            @Override
            public AnnotationKey findApiErrorCode(int annotationCode) {
                return rpcUrlAnnotationKey;
            }
        };
    }


    @Test
    public void fromToFilterTest() {
        ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);
        final short tomcatServiceType = tomcat.getCode();

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode("APP_B", tomcat.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = new FilterDescriptor.Option(null, null);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);


        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo fromSpanBo = new SpanBo();
        fromSpanBo.setApplicationId("APP_A");

        fromSpanBo.setServiceType(tomcatServiceType);
        fromSpanBo.setAgentId("AGENT_A");
        fromSpanBo.setSpanId(100);

        SpanBo toSpanBO = new SpanBo();
        toSpanBO.setApplicationId("APP_B");
        toSpanBO.setServiceType(tomcatServiceType);
        toSpanBO.setAgentId("AGENT_B");
        toSpanBO.setParentSpanId(100);

        SpanBo spanBoC = new SpanBo();
        spanBoC.setApplicationId("APP_C");
        spanBoC.setServiceType(tomcatServiceType);
        spanBoC.setAgentId("AGENT_C");

        Assert.assertTrue(linkFilter.include(Arrays.asList(fromSpanBo, toSpanBO)));
        Assert.assertFalse(linkFilter.include(Arrays.asList(fromSpanBo, spanBoC)));

    }

    @Test
    public void fromToFilterAgentTest() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);
        final short tomcatServiceType = tomcat.getCode();

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), "AGENT_A");
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode("APP_B", tomcat.getName(), "AGENT_B");
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = new FilterDescriptor.Option(null, null);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo fromSpanBo = new SpanBo();
        fromSpanBo.setApplicationId("APP_A");

        fromSpanBo.setServiceType(tomcatServiceType);
        fromSpanBo.setAgentId("AGENT_A");
        fromSpanBo.setSpanId(100);

        SpanBo toSpanBO = new SpanBo();
        toSpanBO.setApplicationId("APP_B");
        toSpanBO.setServiceType(tomcatServiceType);
        toSpanBO.setAgentId("AGENT_B");
        toSpanBO.setParentSpanId(100);

        SpanBo spanBoC = new SpanBo();
        spanBoC.setApplicationId("APP_C");
        spanBoC.setServiceType(tomcatServiceType);
        spanBoC.setAgentId("AGENT_C");

        Assert.assertTrue(linkFilter.include(Arrays.asList(fromSpanBo, toSpanBO)));
        Assert.assertFalse(linkFilter.include(Arrays.asList(fromSpanBo, spanBoC)));
    }

    @Test
    public void userToWasFilter() {
        final ServiceType user = serviceTypeRegistryService.findServiceTypeByName(USER_TYPE_NAME);
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("USER", user.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.Option option = new FilterDescriptor.Option(null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo user_appA = new SpanBo();
        user_appA.setSpanId(1);
        user_appA.setParentSpanId(-1);
        user_appA.setApplicationId("APP_A");
        user_appA.setApplicationServiceType(tomcat.getCode());
        SpanBo appA_appB = new SpanBo();
        appA_appB.setSpanId(2);
        appA_appB.setParentSpanId(1);
        appA_appB.setApplicationId("APP_B");
        appA_appB.setApplicationServiceType(tomcat.getCode());
        SpanBo appB_appA = new SpanBo();
        appB_appA.setSpanId(3);
        appB_appA.setParentSpanId(2);
        appB_appA.setApplicationId("APP_A");
        appB_appA.setApplicationServiceType(tomcat.getCode());

        Assert.assertTrue(linkFilter.include(Collections.singletonList(user_appA)));
        Assert.assertFalse(linkFilter.include(Collections.singletonList(appA_appB)));
        Assert.assertFalse(linkFilter.include(Collections.singletonList(appB_appA)));
        Assert.assertTrue(linkFilter.include(Arrays.asList(user_appA, appA_appB, appB_appA)));
    }

    private LinkFilter newLinkFilter(FilterDescriptor descriptor, FilterHint hint) {
        return new LinkFilter(descriptor, hint, serviceTypeRegistryService, annotationKeyRegistryService);
    }

    @Test
    public void wasToUnknownFilter() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);
        final ServiceType unknown = serviceTypeRegistryService.findServiceTypeByName(UNKNOWN_TYPE_NAME);

        final String rpcHost = "some.domain.name";
        final String rpcUrl = "http://" + rpcHost + "/some/test/path";
        final String urlPattern = "/some/test/**";

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode(rpcHost, unknown.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.Option option = new FilterDescriptor.Option(encodeUrl(urlPattern), null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        // Reject - no rpc span event
        SpanBo spanBo = new SpanBo();
        spanBo.setSpanId(1);
        spanBo.setParentSpanId(-1);
        spanBo.setApplicationId("APP_A");
        spanBo.setApplicationServiceType(tomcat.getCode());
        Assert.assertFalse(linkFilter.include(Collections.singletonList(spanBo)));

        // Accept - has matching rpc span event
        AnnotationBo rpcAnnotation = new AnnotationBo(RPC_ANNOTATION_CODE, rpcUrl);
        SpanEventBo rpcSpanEvent = new SpanEventBo();
        rpcSpanEvent.setServiceType(RPC_TYPE_CODE);
        rpcSpanEvent.setDestinationId(rpcHost);
        rpcSpanEvent.setAnnotationBoList(Collections.singletonList(rpcAnnotation));
        spanBo.addSpanEvent(rpcSpanEvent);
        Assert.assertTrue(linkFilter.include(Collections.singletonList(spanBo)));
    }

    @Test
    public void wasToWasFilter_perfectMatch() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode("APP_B", tomcat.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = new FilterDescriptor.Option(null, null);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        // Accept - perfect match
        SpanBo user_appA = new SpanBo();
        user_appA.setSpanId(1);
        user_appA.setParentSpanId(-1);
        user_appA.setApplicationId("APP_A");
        user_appA.setApplicationServiceType(tomcat.getCode());
        SpanBo appA_appB = new SpanBo();
        appA_appB.setSpanId(2);
        appA_appB.setParentSpanId(1);
        appA_appB.setApplicationId("APP_B");
        appA_appB.setApplicationServiceType(tomcat.getCode());
        Assert.assertTrue(linkFilter.include(Arrays.asList(user_appA, appA_appB)));
    }

    @Test
    public void wasToWasFilter_noMatch() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode("APP_B", tomcat.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = new FilterDescriptor.Option(null, null);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        // Reject - fromNode different
        SpanBo user_appC = new SpanBo();
        user_appC.setSpanId(1);
        user_appC.setParentSpanId(-1);
        user_appC.setApplicationId("APP_C");
        user_appC.setApplicationServiceType(tomcat.getCode());
        SpanBo appC_appB = new SpanBo();
        appC_appB.setSpanId(2);
        appC_appB.setParentSpanId(1);
        appC_appB.setApplicationId("APP_B");
        appC_appB.setApplicationServiceType(tomcat.getCode());
        Assert.assertFalse(linkFilter.include(Arrays.asList(user_appC, appC_appB)));

        // Reject - toNode different
        SpanBo user_appA = new SpanBo();
        user_appA.setSpanId(1);
        user_appA.setParentSpanId(-1);
        user_appA.setApplicationId("APP_A");
        user_appA.setApplicationServiceType(tomcat.getCode());
        SpanBo appA_appC = new SpanBo();
        appA_appC.setSpanId(2);
        appA_appC.setParentSpanId(1);
        appA_appC.setApplicationId("APP_C");
        appA_appC.setApplicationServiceType(tomcat.getCode());
        Assert.assertFalse(linkFilter.include(Arrays.asList(user_appA, appA_appC)));
    }

    @Test
    public void wasToWasFilter_noMatch_missingReceivingSpan() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);

        final String rpcHost = "some.domain.name";
        final String rpcUrl = "http://" + rpcHost + "/some/test/path";

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode("APP_B", tomcat.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = mock(FilterDescriptor.Option.class);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);


        FilterHint emptyHint = new FilterHint(Collections.emptyList());
        FilterHint unmatchingHint = new FilterHint(Collections.singletonList(
                new RpcHint("APP_B", Collections.singletonList(
                        new RpcType("different.domain.name", RPC_TYPE_CODE)))));
        FilterHint matchingHint = new FilterHint(Collections.singletonList(
                new RpcHint("APP_B", Collections.singletonList(
                        new RpcType(rpcHost, RPC_TYPE_CODE)))));

        LinkFilter emptyHintLinkFilter = newLinkFilter(descriptor, emptyHint);
        LinkFilter unmatchingHintLinkFilter = newLinkFilter(descriptor, unmatchingHint);
        LinkFilter matchingHintLinkFilter = newLinkFilter(descriptor, matchingHint);
        logger.debug("emptyHintLinkFilter : {}", emptyHintLinkFilter.toString());
        logger.debug("unmatchingHintLinkFilter : {}", unmatchingHintLinkFilter.toString());
        logger.debug("matchingHintLinkFilter : {}", matchingHintLinkFilter.toString());

        SpanBo fromSpan = new SpanBo();
        fromSpan.setSpanId(1);
        fromSpan.setParentSpanId(-1);
        fromSpan.setApplicationId("APP_A");
        fromSpan.setApplicationServiceType(tomcat.getCode());
        AnnotationBo rpcAnnotation = new AnnotationBo(RPC_ANNOTATION_CODE, rpcUrl);
        SpanEventBo rpcSpanEvent = new SpanEventBo();
        rpcSpanEvent.setServiceType(RPC_TYPE_CODE);
        rpcSpanEvent.setDestinationId(rpcHost);
        rpcSpanEvent.setAnnotationBoList(Collections.singletonList(rpcAnnotation));
        fromSpan.addSpanEvent(rpcSpanEvent);
        // Reject - filter hint empty
        Assert.assertFalse(emptyHintLinkFilter.include(Collections.singletonList(fromSpan)));
        // Reject - filter hint does not match
        Assert.assertFalse(unmatchingHintLinkFilter.include(Collections.singletonList(fromSpan)));
        // Accept - filter hint matches
        Assert.assertTrue(matchingHintLinkFilter.include(Collections.singletonList(fromSpan)));

        // Check rpc url as well
        final String unmatchingUrlPattern = "/other/test/**";
        final String matchingUrlPattern = "/some/test/**";
        // Reject - url pattern does not match
        when(option.getUrlPattern()).thenReturn(unmatchingUrlPattern);
        LinkFilter matchingHintLinkFilterWithUnmatchingUrlPattern = newLinkFilter(descriptor, matchingHint);
        Assert.assertFalse(matchingHintLinkFilterWithUnmatchingUrlPattern.include(Collections.singletonList(fromSpan)));
        // Accept - url pattern matches
        when(option.getUrlPattern()).thenReturn(matchingUrlPattern);
        LinkFilter matchingHintLinkFilterWithMatchingUrlPattern = newLinkFilter(descriptor, matchingHint);
        Assert.assertTrue(matchingHintLinkFilterWithMatchingUrlPattern.include(Collections.singletonList(fromSpan)));
    }

    @Test
    public void wasToBackendFilter() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);
        final ServiceType backend = serviceTypeRegistryService.findServiceTypeByName(BACKEND_TYPE_NAME);

        final String destinationA = "BACKEND_A";
        final String destinationB = "BACKEND_B";

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode(destinationA, backend.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = mock(FilterDescriptor.Option.class);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo matchingSpan = new SpanBo();
        matchingSpan.setApplicationId("APP_A");
        matchingSpan.setApplicationServiceType(tomcat.getCode());
        SpanEventBo spanEventDestinationA = new SpanEventBo();
        spanEventDestinationA.setDestinationId(destinationA);
        spanEventDestinationA.setServiceType(BACKEND_TYPE_CODE);
        matchingSpan.addSpanEvent(spanEventDestinationA);
        Assert.assertTrue(linkFilter.include(Collections.singletonList(matchingSpan)));

        SpanBo unmatchingSpan = new SpanBo();
        unmatchingSpan.setApplicationId("APP_A");
        unmatchingSpan.setApplicationServiceType(tomcat.getCode());
        SpanEventBo spanEventDestinationB = new SpanEventBo();
        spanEventDestinationB.setDestinationId(destinationB);
        spanEventDestinationB.setServiceType(BACKEND_TYPE_CODE);
        unmatchingSpan.addSpanEvent(spanEventDestinationB);
        Assert.assertFalse(linkFilter.include(Collections.singletonList(unmatchingSpan)));

        Assert.assertTrue(linkFilter.include(Arrays.asList(matchingSpan, unmatchingSpan)));

        SpanBo bothSpan = new SpanBo();
        bothSpan.setApplicationId("APP_A");
        bothSpan.setApplicationServiceType(tomcat.getCode());
        bothSpan.addSpanEventBoList(Arrays.asList(spanEventDestinationA, spanEventDestinationB));
        Assert.assertTrue(linkFilter.include(Collections.singletonList(bothSpan)));
    }

    @Test
    public void wasToQueueFilter() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);
        final ServiceType messageQueue = serviceTypeRegistryService.findServiceTypeByName(MESSAGE_QUEUE_TYPE_NAME);

        final String messageQueueA = "QUEUE_A";
        final String messageQueueB = "QUEUE_B";

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode(messageQueueA, messageQueue.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = mock(FilterDescriptor.Option.class);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo matchingSpan = new SpanBo();
        matchingSpan.setApplicationId("APP_A");
        matchingSpan.setApplicationServiceType(tomcat.getCode());
        SpanEventBo spanEventDestinationA = new SpanEventBo();
        spanEventDestinationA.setDestinationId(messageQueueA);
        spanEventDestinationA.setServiceType(MESSAGE_QUEUE_TYPE_CODE);
        matchingSpan.addSpanEvent(spanEventDestinationA);
        Assert.assertTrue(linkFilter.include(Collections.singletonList(matchingSpan)));

        SpanBo unmatchingSpan = new SpanBo();
        unmatchingSpan.setApplicationId("APP_A");
        unmatchingSpan.setApplicationServiceType(tomcat.getCode());
        SpanEventBo spanEventDestinationB = new SpanEventBo();
        spanEventDestinationB.setDestinationId(messageQueueB);
        spanEventDestinationB.setServiceType(MESSAGE_QUEUE_TYPE_CODE);
        unmatchingSpan.addSpanEvent(spanEventDestinationB);
        Assert.assertFalse(linkFilter.include(Collections.singletonList(unmatchingSpan)));

        Assert.assertTrue(linkFilter.include(Arrays.asList(matchingSpan, unmatchingSpan)));

        SpanBo bothSpan = new SpanBo();
        bothSpan.setApplicationId("APP_A");
        bothSpan.setApplicationServiceType(tomcat.getCode());
        bothSpan.addSpanEventBoList(Arrays.asList(spanEventDestinationA, spanEventDestinationB));
        Assert.assertTrue(linkFilter.include(Collections.singletonList(bothSpan)));
    }

    @Test
    public void queueToWasFilter() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName(TOMCAT_TYPE_NAME);
        final ServiceType messageQueue = serviceTypeRegistryService.findServiceTypeByName(MESSAGE_QUEUE_TYPE_NAME);

        final String messageQueueA = "QUEUE_A";
        final String messageQueueB = "QUEUE_B";

        FilterDescriptor.FromNode fromNode = new FilterDescriptor.FromNode(messageQueueA, messageQueue.getName(), null);
        FilterDescriptor.ToNode toNode = new FilterDescriptor.ToNode("APP_A", tomcat.getName(), null);
        FilterDescriptor.SelfNode selfNode = new FilterDescriptor.SelfNode(null, null, null);
        FilterDescriptor.ResponseTime responseTime = new FilterDescriptor.ResponseTime(null, null);
        FilterDescriptor.Option option = mock(FilterDescriptor.Option.class);
        FilterDescriptor descriptor = new FilterDescriptor(fromNode, toNode, selfNode, responseTime, option);

        FilterHint hint = new FilterHint(Collections.emptyList());

        LinkFilter linkFilter = newLinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo matchingSpan = new SpanBo();
        matchingSpan.setApplicationId("APP_A");
        matchingSpan.setApplicationServiceType(tomcat.getCode());
        matchingSpan.setAcceptorHost(messageQueueA);
        Assert.assertTrue(linkFilter.include(Collections.singletonList(matchingSpan)));

        SpanBo unmatchingSpan = new SpanBo();
        unmatchingSpan.setApplicationId("APP_A");
        unmatchingSpan.setApplicationServiceType(tomcat.getCode());
        unmatchingSpan.setAcceptorHost(messageQueueB);
        Assert.assertFalse(linkFilter.include(Collections.singletonList(unmatchingSpan)));
    }

    private String encodeUrl(String value) {
        return Base64.encodeBytes(value.getBytes(StandardCharsets.UTF_8));
    }

}