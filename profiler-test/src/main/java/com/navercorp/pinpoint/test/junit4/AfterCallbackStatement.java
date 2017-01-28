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

/**
 * @author emeroad
 */
public class AfterCallbackStatement extends Statement {
    private final Statement statement;
    private final Statement after;

    public AfterCallbackStatement(Statement statement, Statement after) {
        if (statement == null) {
            throw new NullPointerException("statement must not be null");
        }
        if (after == null) {
            throw new NullPointerException("AFTER must not be null");
        }
        this.statement = statement;
        this.after = after;
    }


    @Override
    public void evaluate() throws Throwable {
        try {
            statement.evaluate();
        } finally {
            after.evaluate();
        }
    }
}
