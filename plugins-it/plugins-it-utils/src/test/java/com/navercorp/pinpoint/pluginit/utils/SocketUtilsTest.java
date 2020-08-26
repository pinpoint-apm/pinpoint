/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.pluginit.utils;

import org.junit.Test;

import java.util.SortedSet;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author WonChul Heo(heowc)
 */
public class SocketUtilsTest {

    @Test
    public void testSuccessfulFindAvailableTcpPort() {
        assertThat(SocketUtils.findAvailableTcpPort(),
                allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX)));
        assertThat(SocketUtils.findAvailableTcpPort(SocketUtils.PORT_RANGE_MIN + 1),
                allOf(greaterThan(SocketUtils.PORT_RANGE_MIN + 1), lessThan(SocketUtils.PORT_RANGE_MAX)));
        assertThat(SocketUtils.findAvailableTcpPort(SocketUtils.PORT_RANGE_MIN, SocketUtils.PORT_RANGE_MAX - 1),
                allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX - 1)));
        assertThat(SocketUtils.findAvailableTcpPort(1, SocketUtils.PORT_RANGE_MAX),
                allOf(greaterThan(1), lessThan(SocketUtils.PORT_RANGE_MAX)));
    }

    @Test
    public void testFailureFindAvailableTcpPort() {
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPort(0, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'minPort' must be greater than 0");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPort(SocketUtils.PORT_RANGE_MAX, SocketUtils.PORT_RANGE_MIN);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPort(SocketUtils.PORT_RANGE_MAX, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPort(SocketUtils.PORT_RANGE_MIN, SocketUtils.PORT_RANGE_MAX + 1);
            }
        }, IllegalArgumentException.class, "'maxPort' must be less than or equal to " + SocketUtils.PORT_RANGE_MAX);
    }

    @Test
    public void testSuccessfulFindAvailableTcpPorts() {
        SortedSet<Integer> availableTcpPorts = SocketUtils.findAvailableTcpPorts(1);
        assertThat(availableTcpPorts.size(), is(1));
        for (Integer port : availableTcpPorts) {
            assertThat(port, allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX)));
        }

        availableTcpPorts = SocketUtils.findAvailableTcpPorts(2);
        assertThat(availableTcpPorts.size(), is(2));
        for (Integer port : availableTcpPorts) {
            assertThat(port, allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX)));
        }

        availableTcpPorts = SocketUtils.findAvailableTcpPorts(3, SocketUtils.PORT_RANGE_MIN + 1, SocketUtils.PORT_RANGE_MAX - 1);
        assertThat(availableTcpPorts.size(), is(3));
        for (Integer port : availableTcpPorts) {
            assertThat(port, allOf(greaterThan(SocketUtils.PORT_RANGE_MIN + 1), lessThan(SocketUtils.PORT_RANGE_MAX - 1)));
        }
    }

    @Test
    public void testFailureFindAvailableTcpPorts() {
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPorts(0);
            }
        }, IllegalArgumentException.class, "'numRequested' must be greater than 0");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPorts(-1);
            }
        }, IllegalArgumentException.class, "'numRequested' must be greater than 0");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPorts(1, SocketUtils.PORT_RANGE_MAX, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPorts(1, SocketUtils.PORT_RANGE_MAX + 1, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableTcpPorts(2, SocketUtils.PORT_RANGE_MAX - 1, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'numRequested' must not be greater than 'maxPort' - 'minPort'");
    }

    @Test
    public void testSuccessfulFindAvailableUdpPort() {
        assertThat(SocketUtils.findAvailableUdpPort(),
                allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX)));
        assertThat(SocketUtils.findAvailableUdpPort(SocketUtils.PORT_RANGE_MIN + 1),
                allOf(greaterThan(SocketUtils.PORT_RANGE_MIN + 1), lessThan(SocketUtils.PORT_RANGE_MAX)));
        assertThat(SocketUtils.findAvailableUdpPort(SocketUtils.PORT_RANGE_MIN, SocketUtils.PORT_RANGE_MAX - 1),
                allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX - 1)));
        assertThat(SocketUtils.findAvailableUdpPort(1, SocketUtils.PORT_RANGE_MAX),
                allOf(greaterThan(1), lessThan(SocketUtils.PORT_RANGE_MAX)));
    }

    @Test
    public void testFailureFindAvailableUdpPort() {
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPort(0, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'minPort' must be greater than 0");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPort(SocketUtils.PORT_RANGE_MAX, SocketUtils.PORT_RANGE_MIN);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPort(SocketUtils.PORT_RANGE_MAX, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPort(SocketUtils.PORT_RANGE_MIN, SocketUtils.PORT_RANGE_MAX + 1);
            }
        }, IllegalArgumentException.class, "'maxPort' must be less than or equal to " + SocketUtils.PORT_RANGE_MAX);
    }

    @Test
    public void testSuccessfulFindAvailableUdpPorts() {
        SortedSet<Integer> availableTcpPorts = SocketUtils.findAvailableUdpPorts(1);
        assertThat(availableTcpPorts.size(), is(1));
        for (Integer port : availableTcpPorts) {
            assertThat(port, allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX)));
        }

        availableTcpPorts = SocketUtils.findAvailableUdpPorts(2);
        assertThat(availableTcpPorts.size(), is(2));
        for (Integer port : availableTcpPorts) {
            assertThat(port, allOf(greaterThan(SocketUtils.PORT_RANGE_MIN), lessThan(SocketUtils.PORT_RANGE_MAX)));
        }

        availableTcpPorts = SocketUtils.findAvailableUdpPorts(3, SocketUtils.PORT_RANGE_MIN + 1, SocketUtils.PORT_RANGE_MAX - 1);
        assertThat(availableTcpPorts.size(), is(3));
        for (Integer port : availableTcpPorts) {
            assertThat(port, allOf(greaterThan(SocketUtils.PORT_RANGE_MIN + 1), lessThan(SocketUtils.PORT_RANGE_MAX - 1)));
        }
    }

    @Test
    public void testFailureFindAvailableUdpPorts() {
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPorts(0);
            }
        }, IllegalArgumentException.class, "'numRequested' must be greater than 0");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPorts(-1);
            }
        }, IllegalArgumentException.class, "'numRequested' must be greater than 0");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPorts(1, SocketUtils.PORT_RANGE_MAX, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPorts(1, SocketUtils.PORT_RANGE_MAX + 1, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'maxPort' must be greater than 'minPort'");
        assertThrows(new Runnable() {
            @Override
            public void run() {
                SocketUtils.findAvailableUdpPorts(2, SocketUtils.PORT_RANGE_MAX - 1, SocketUtils.PORT_RANGE_MAX);
            }
        }, IllegalArgumentException.class, "'numRequested' must not be greater than 'maxPort' - 'minPort'");
    }

    private static void assertThrows(Runnable runnable, Class clazz, String message) {
        try {
            runnable.run();
            fail();
        } catch (RuntimeException e) {
            if (clazz.isInstance(e)) {
                assertThat(e, isA(clazz));
                assertThat(e.getMessage(), is(message));
            } else {
                throw e;
            }
        }
    }
}
