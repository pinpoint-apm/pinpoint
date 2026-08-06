/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.reactorsupport;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * classpath-negative pin (W9 / doc 18 §4.7): what happens when a wrap gate is enabled but
 * reactor-core is NOT on the application classpath.
 *
 * <p>Defining the (shaded) {@code SeamPublisherWrapper} already requires reactor types — class
 * verification resolves them — so the failure is a {@link NoClassDefFoundError} raised the
 * moment the class is first needed. That moment is safe by construction: the interceptor class
 * itself carries no reactor types in its own signatures, so weaving and interceptor construction
 * succeed, and the wrapper is first referenced from inside the interceptor's
 * {@code catch (Throwable)}-guarded call path, which returns the original result (pinned
 * per-interceptor in the plugins, e.g. redisson's {@code WrappingReactiveMethodInterceptorTest}).
 * The call degrades to untraced (with a warn per call), never broken.
 *
 * <p>Realism note: lettuce-core and redisson both declare reactor-core as a required dependency,
 * and the spring-tx reactive seam only fires with a ReactiveTransactionManager — the negative
 * classpath needs a deliberate exclusion plus the opt-in flag. This test is the cheap insurance
 * that even then the agent degrades instead of breaking the call.
 */
public class ReactorAbsenceClassLoadingTest {

    private static final String SUPPORT_PACKAGE = "com.navercorp.pinpoint.plugin.reactorsupport.";

    @Test
    public void withoutReactor_wrapperFailsAsErrorAtFirstResolution() {
        final ReactorHidingClassLoader classLoader = new ReactorHidingClassLoader();

        // the very first resolution of the wrapper class fails as an Error (not an exception) -
        // exactly what the wrapping interceptors' catch(Throwable) must and does contain.
        assertThrows(NoClassDefFoundError.class,
                () -> Class.forName(SUPPORT_PACKAGE + "SeamPublisherWrapper", true, classLoader));
    }

    /**
     * Delegates everything to the test classpath EXCEPT reactor (hidden, as if absent) and the
     * seam-support package itself (redefined here so its reactor references resolve against this
     * loader) - the same visibility a shaded plugin copy has in an app without reactor.
     */
    private static final class ReactorHidingClassLoader extends ClassLoader {
        ReactorHidingClassLoader() {
            super(ReactorAbsenceClassLoadingTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("reactor.") || name.startsWith("org.reactivestreams.")) {
                throw new ClassNotFoundException(name + " (hidden: simulating reactor absence)");
            }
            if (name.startsWith(SUPPORT_PACKAGE)) {
                synchronized (getClassLoadingLock(name)) {
                    final Class<?> loaded = findLoadedClass(name);
                    if (loaded != null) {
                        return loaded;
                    }
                    final byte[] bytes = readClassBytes(name);
                    final Class<?> defined = defineClass(name, bytes, 0, bytes.length);
                    if (resolve) {
                        resolveClass(defined);
                    }
                    return defined;
                }
            }
            return super.loadClass(name, resolve);
        }

        private byte[] readClassBytes(String name) throws ClassNotFoundException {
            final String resource = name.replace('.', '/') + ".class";
            try (InputStream in = getParent().getResourceAsStream(resource)) {
                if (in == null) {
                    throw new ClassNotFoundException(name);
                }
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                return out.toByteArray();
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }
}
