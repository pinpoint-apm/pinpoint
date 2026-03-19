package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.util.ServiceTypeRegistryMockFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceNodeNameParserTest {

    private ServiceTypeRegistryService newServiceTypeRegistry() {
        ServiceTypeRegistryMockFactory factory = new ServiceTypeRegistryMockFactory();
        factory.addServiceTypeMock(ServiceType.STAND_ALONE);
        factory.addServiceTypeMock(ServiceType.UNKNOWN);
        return factory.createMockServiceTypeRegistryService();
    }

    @Test
    void parse() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        ServiceNodeName parsed = new ServiceNodeNameParser(registry).parse("myService^myApp^STAND_ALONE");
        assertEquals(new ServiceNodeName("myService", "myApp", ServiceType.STAND_ALONE), parsed);
    }

    @Test
    void parse_withEscapedApplicationName() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        ServiceNodeName parsed = new ServiceNodeNameParser(registry).parse("svc^my\\^app^UNKNOWN");
        assertEquals(new ServiceNodeName("svc", "my^app", ServiceType.UNKNOWN), parsed);
    }

    @Test
    void parse_roundTrip() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        ServiceNodeName original = new ServiceNodeName("svc", "my^app", ServiceType.STAND_ALONE);
        ServiceNodeName parsed = new ServiceNodeNameParser(registry).parse(original.getName());
        assertEquals(original, parsed);
    }

    @Test
    void parse_nullServiceNodeName() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        assertThrows(NullPointerException.class, () -> new ServiceNodeNameParser(registry).parse(null));
    }

    @Test
    void parse_nullServiceTypeRegistry() {
        assertThrows(NullPointerException.class, () -> new ServiceNodeNameParser(null));
    }

    @Test
    void parse_noDelimiter() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        assertThrows(IllegalArgumentException.class, () -> new ServiceNodeNameParser(registry).parse("invalidString"));
    }

    @Test
    void parse_unknownServiceType() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        assertThrows(IllegalArgumentException.class, () -> new ServiceNodeNameParser(registry).parse("svc^app^NONEXISTENT"));
    }

    @Test
    void parse_roundTrip_withBackslashAndCaret() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        ServiceNodeName original = new ServiceNodeName("svc", "a\\^b", ServiceType.STAND_ALONE);
        ServiceNodeName parsed = new ServiceNodeNameParser(registry).parse(original.getName());
        assertEquals(original, parsed);
    }

    @Test
    void parse_roundTrip_withTrailingBackslash() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        ServiceNodeName original = new ServiceNodeName("svc", "app\\", ServiceType.UNKNOWN);
        ServiceNodeName parsed = new ServiceNodeNameParser(registry).parse(original.getName());
        assertEquals(original, parsed);
    }

    @Test
    void parse_roundTrip_multipleEscapeChars() {
        ServiceTypeRegistryService registry = newServiceTypeRegistry();
        ServiceNodeName original = new ServiceNodeName("svc", "a^b\\^c^d", ServiceType.STAND_ALONE);
        ServiceNodeName parsed = new ServiceNodeNameParser(registry).parse(original.getName());
        assertEquals(original, parsed);
    }
}
