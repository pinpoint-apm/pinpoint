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

package com.navercorp.pinpoint.test.junit4;

import org.junit.runners.model.Statement;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ContextClassLoaderStatement extends Statement {
    private final Statement delegate;
    private final ClassLoader classLoader;

    public ContextClassLoaderStatement(Statement delegate, ClassLoader classLoader) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    @Override
    public void evaluate() throws Throwable {
        final Thread thread = Thread.currentThread();
        final ClassLoader backup = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            delegate.evaluate();
        } finally {
            thread.setContextClassLoader(backup);
        }
    }

    public static Statement wrap(Statement statement, ClassLoader classLoader) {
        Objects.requireNonNull(statement, "statement");
        Objects.requireNonNull(classLoader, "classLoader");

        return new ContextClassLoaderStatement(statement, classLoader);

    }
}
