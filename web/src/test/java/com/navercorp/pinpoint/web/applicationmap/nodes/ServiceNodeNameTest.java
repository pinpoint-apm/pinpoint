package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceNodeNameTest {

    @Test
    void constructor() {
        ServiceNodeName nodeName = new ServiceNodeName("myService", "myApp", ServiceType.STAND_ALONE);
        assertEquals("myService^myApp^STAND_ALONE", nodeName.getName());
    }

    @Test
    void constructor_nullServiceName() {
        assertThrows(NullPointerException.class,
                () -> new ServiceNodeName(null, "myApp", ServiceType.STAND_ALONE));
    }

    @Test
    void constructor_nullApplicationName() {
        assertThrows(NullPointerException.class,
                () -> new ServiceNodeName("myService", null, ServiceType.STAND_ALONE));
    }

    @Test
    void constructor_nullServiceType() {
        assertThrows(NullPointerException.class,
                () -> new ServiceNodeName("myService", "myApp", null));
    }

    @Test
    void of() {
        Service service = new Service("myService", 1);
        Application application = new Application(service, "myApp", ServiceType.STAND_ALONE);

        ServiceNodeName nodeName = ServiceNodeName.of(application);
        assertEquals("myService^myApp^STAND_ALONE", nodeName.getName());
    }

    @Test
    void of_nullApplication() {
        assertThrows(NullPointerException.class, () -> ServiceNodeName.of(null));
    }

    @Test
    void getName() {
        ServiceNodeName nodeName = new ServiceNodeName("svc", "app", ServiceType.UNKNOWN);
        String expected = "svc^app^UNKNOWN";
        assertEquals(expected, nodeName.getName());
    }

    @Test
    void toServiceNodeName() {
        String result = ServiceNodeName.toServiceNodeName("svc", "app", ServiceType.STAND_ALONE);
        assertEquals("svc^app^STAND_ALONE", result);
    }

    @Test
    void toServiceNodeKey() {
        String result = ServiceNodeName.toServiceNodeKey("svc", "app", ServiceType.STAND_ALONE);
        // toServiceNodeKey uses serviceType.getName() instead of getDesc()
        assertEquals("svc^app^STAND_ALONE", result);
    }

    @Test
    void escapeApplicationName() {
        // Application name containing the NODE_DELIMITER "^" should be escaped
        String escaped = ServiceNodeName.escapeApplicationName("my^app");
        assertEquals("my\\^app", escaped);
    }

    @Test
    void escapeApplicationName_noDelimiter() {
        String escaped = ServiceNodeName.escapeApplicationName("myapp");
        assertEquals("myapp", escaped);
    }

    @Test
    void escapeApplicationName_multipleDelimiters() {
        String escaped = ServiceNodeName.escapeApplicationName("a^b^c");
        assertEquals("a\\^b\\^c", escaped);
    }

    @Test
    void toServiceNodeName_withEscape() {
        String result = ServiceNodeName.toServiceNodeName("svc", "my^app", ServiceType.UNKNOWN);
        assertEquals("svc^my\\^app^UNKNOWN", result);
    }

    @Test
    void toServiceNodeKey_withEscape() {
        String result = ServiceNodeName.toServiceNodeKey("svc", "my^app", ServiceType.UNKNOWN);
        assertEquals("svc^my\\^app^UNKNOWN", result);
    }

    @Test
    void equals_same() {
        ServiceNodeName name1 = new ServiceNodeName("svc", "app", ServiceType.STAND_ALONE);
        ServiceNodeName name2 = new ServiceNodeName("svc", "app", ServiceType.STAND_ALONE);
        assertEquals(name1, name2);
        assertEquals(name1.hashCode(), name2.hashCode());
    }

    @Test
    void equals_differentServiceName() {
        ServiceNodeName name1 = new ServiceNodeName("svc1", "app", ServiceType.STAND_ALONE);
        ServiceNodeName name2 = new ServiceNodeName("svc2", "app", ServiceType.STAND_ALONE);
        assertNotEquals(name1, name2);
    }

    @Test
    void equals_differentApplicationName() {
        ServiceNodeName name1 = new ServiceNodeName("svc", "app1", ServiceType.STAND_ALONE);
        ServiceNodeName name2 = new ServiceNodeName("svc", "app2", ServiceType.STAND_ALONE);
        assertNotEquals(name1, name2);
    }

    @Test
    void equals_differentServiceType() {
        ServiceNodeName name1 = new ServiceNodeName("svc", "app", ServiceType.STAND_ALONE);
        ServiceNodeName name2 = new ServiceNodeName("svc", "app", ServiceType.UNKNOWN);
        assertNotEquals(name1, name2);
    }

    @Test
    void equals_null() {
        ServiceNodeName name1 = new ServiceNodeName("svc", "app", ServiceType.STAND_ALONE);
        assertNotEquals(null, name1);
    }

    @Test
    void equals_differentClass() {
        ServiceNodeName name1 = new ServiceNodeName("svc", "app", ServiceType.STAND_ALONE);
        assertNotEquals("string", name1);
    }

    @Test
    void testToString() {
        ServiceNodeName nodeName = new ServiceNodeName("svc", "app", ServiceType.UNKNOWN);
        assertEquals(nodeName.getName(), nodeName.toString());
    }

    @Test
    void escapeApplicationName_withBackslash() {
        // applicationName containing "\" should be preserved as-is (only "^" gets escaped)
        String escaped = ServiceNodeName.escapeApplicationName("my\\app");
        assertEquals("my\\app", escaped);
    }

    @Test
    void escapeApplicationName_withBackslashAndCaret() {
        // applicationName = "a\^b" (backslash + caret): "^" gets escaped to "\^", so "\^" becomes "\\^"
        String escaped = ServiceNodeName.escapeApplicationName("a\\^b");
        assertEquals("a\\\\^b", escaped);
    }
}

