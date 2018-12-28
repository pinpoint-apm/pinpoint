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

package com.navercorp.pinpoint.plugins.loader.trace.yaml;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ParsedTraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.plugins.loader.trace.TraceMetadataProviderParser;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataProviderYamlParser implements TraceMetadataProviderParser {

    private static final String TYPE_PROVIDER_DEF_ENTRY = "META-INF/pinpoint/type-provider.yml";

    private final String typeProviderDefEntry;

    public TraceMetadataProviderYamlParser() {
        this.typeProviderDefEntry = TYPE_PROVIDER_DEF_ENTRY;
    }

    @VisibleForTesting
    TraceMetadataProviderYamlParser(String typeProviderDefEntry) {
        this.typeProviderDefEntry = typeProviderDefEntry;
    }

    @Override
    public List<TraceMetadataProvider> parse(ClassLoader classLoader) {

        List<TraceMetadataProvider> traceMetadataProviders = new ArrayList<TraceMetadataProvider>();
        try {
            Enumeration<URL> metaUrls = classLoader.getResources(typeProviderDefEntry);
            while (metaUrls.hasMoreElements()) {
                URL metaUrl = metaUrls.nextElement();
                ParsedTraceMetadata parsedTraceMetadata = parse(metaUrl);
                if (parsedTraceMetadata != null) {
                    try {
                        String name = parseJarName(metaUrl);
                        List<ServiceTypeInfo> serviceTypeInfos = toServiceTypeInfos(parsedTraceMetadata.getServiceTypes());
                        List<AnnotationKey> annotationKeys = toAnnotationKeys(parsedTraceMetadata.getAnnotationKeys());
                        TraceMetadataProvider traceMetadataProvider = new ParsedTraceMetadataProvider(name, serviceTypeInfos, annotationKeys);
                        traceMetadataProviders.add(traceMetadataProvider);
                    } catch (Exception e) {
                        throw new IllegalStateException("Invalid type provider definition: " + metaUrl.toString(), e);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("IOException parsing " + typeProviderDefEntry + " files", e);
        }
        return traceMetadataProviders;
    }

    private ParsedTraceMetadata parse(URL metaUrl) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = metaUrl.openStream();
            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, ParsedTraceMetadata.class);
        } catch (IOException e) {
            throw new IllegalStateException("Error opening stream: " + metaUrl.toString(), e);
        } catch (YAMLException e) {
            throw new IllegalStateException("Error parsing yml: " + metaUrl.toString(), e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private String parseJarName(URL metaUrl) {
        try {
            URLConnection urlConnection = metaUrl.openConnection();
            if (urlConnection instanceof JarURLConnection) {
                String file = ((JarURLConnection) urlConnection).getJarFileURL().getFile();
                int separatorIdx = file.lastIndexOf("/");
                if (separatorIdx < 0 || separatorIdx + 1 >= file.length()) {
                    return file;
                }
                return file.substring(separatorIdx + 1);
            }
        } catch (IOException e) {
            return metaUrl.toExternalForm();
        }
        return metaUrl.toExternalForm();
    }

    private List<ServiceTypeInfo> toServiceTypeInfos(List<ParsedServiceType> parsedServiceTypes) {
        if (CollectionUtils.isEmpty(parsedServiceTypes)) {
            return Collections.emptyList();
        }
        List<ServiceTypeInfo> serviceTypeInfos = new ArrayList<ServiceTypeInfo>(parsedServiceTypes.size());
        for (ParsedServiceType parsedServiceType : parsedServiceTypes) {
            serviceTypeInfos.add(parsedServiceType.toServiceTypeInfo());
        }
        return serviceTypeInfos;
    }

    private List<AnnotationKey> toAnnotationKeys(List<ParsedAnnotationKey> parsedAnnotationKeys) {
        if (CollectionUtils.isEmpty(parsedAnnotationKeys)) {
            return Collections.emptyList();
        }
        List<AnnotationKey> annotationKeys = new ArrayList<AnnotationKey>(parsedAnnotationKeys.size());
        for (ParsedAnnotationKey parsedAnnotationKey : parsedAnnotationKeys) {
            annotationKeys.add(parsedAnnotationKey.toAnnotationKey());
        }
        return annotationKeys;
    }
}
