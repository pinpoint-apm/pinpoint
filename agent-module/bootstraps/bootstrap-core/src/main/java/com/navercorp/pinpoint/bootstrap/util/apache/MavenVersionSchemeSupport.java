/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.navercorp.pinpoint.bootstrap.util.apache;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

// copy 2021.09.08 -> master branch : b06469053450eafee03aae287cc4ae3a1f4d9645
// https://github.com/apache/maven-resolver/commits/master
public class MavenVersionSchemeSupport {

    public List<MavenVersionRange> parseVersionConstraint(final String constraint)
            throws InvalidVersionSpecificationException {
        String process = requireNonNull(constraint, "constraint cannot be null");

        List<MavenVersionRange> ranges = new ArrayList<>();

        while (process.startsWith("[") || process.startsWith("(")) {
            int index1 = process.indexOf(')');
            int index2 = process.indexOf(']');

            int index = index2;
            if (index2 < 0 || (index1 >= 0 && index1 < index2)) {
                index = index1;
            }

            if (index < 0) {
                throw new InvalidVersionSpecificationException(constraint, "Unbounded version range " + constraint);
            }

            MavenVersionRange range = new MavenVersionRange(process.substring(0, index + 1));
            ranges.add(range);

            process = process.substring(index + 1).trim();

            if (process.startsWith(",")) {
                process = process.substring(1).trim();
            }
        }

        if (!process.isEmpty() && !ranges.isEmpty()) {
            throw new InvalidVersionSpecificationException(
                    constraint, "Invalid version range " + constraint + ", expected [ or ( but got " + process);
        }

        return ranges;
    }
}
