/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ArtifactIdUtils {

    private static final String ARTIFACT_DELIMITER = ":";

    public static final String ARTIFACT_SEPARATOR = ";";

    public static List<Artifact> toArtifact(String[] artifactNameArray) {
        if (artifactNameArray == null) {
            return Collections.emptyList();
        }

        List<Artifact> result = new ArrayList<Artifact>(artifactNameArray.length);
        for (String artifactName : artifactNameArray) {
            Artifact artifact = toArtifact(artifactName);
            if (artifact != null) {
                result.add(artifact);
            }
        }

        return result;
    }

    public static Artifact toArtifact(String artifactName) {
        if (artifactName == null) {
            return null;
        }

        String[] splitValue = artifactName.split(ARTIFACT_DELIMITER);
        if (splitValue == null) {
            return null;
        }

        if (splitValue.length == 3) {
            return new DefaultArtifact(splitValue[0], splitValue[1], "jar", splitValue[2]);
        } else if (splitValue.length == 4) {
            return new DefaultArtifact(splitValue[0], splitValue[1], splitValue[3], "jar", splitValue[2]);
        }

        return null;
    }

    public static String artifactToString(Artifact artifact) {
        StringBuilder result = new StringBuilder();

        String groupId = artifact.getGroupId();
        result.append(groupId).append(ARTIFACT_DELIMITER);

        String artifactId = artifact.getArtifactId();
        result.append(artifactId).append(ARTIFACT_DELIMITER);

        String version = artifact.getVersion();
        result.append(version);

        if (StringUtils.hasText(artifact.getClassifier())) {
            String classifier = artifact.getClassifier();
            result.append(ARTIFACT_DELIMITER).append(classifier);
        }


        return result.toString();
    }

}
