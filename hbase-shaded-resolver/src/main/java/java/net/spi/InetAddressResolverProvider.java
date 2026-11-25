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

package java.net.spi;

/**
 * Compile-only stub of the JDK 18 API. Not packaged: the class of the JDK is used at runtime.
 */
public abstract class InetAddressResolverProvider {

    protected InetAddressResolverProvider() {
    }

    public abstract InetAddressResolver get(Configuration configuration);

    public abstract String name();

    public interface Configuration {
        InetAddressResolver builtinResolver();

        String lookupLocalHostName();
    }
}
