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

package com.navercorp.pinpoint.test.junit4;

import org.junit.runners.model.Statement;

import java.util.Objects;

/**
 * @author emeroad
 */
public class BeforeCallbackStatement extends Statement {

    private final Statement before;
    private final Statement statement;

    public BeforeCallbackStatement(Statement statement, Statement before) {
        this.statement = Objects.requireNonNull(statement, "statement");
        this.before = Objects.requireNonNull(before, "before");
    }

    @Override
    public void evaluate() throws Throwable {
        this.before.evaluate();
        this.statement.evaluate();
    }

}
