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

package com.navercorp.pinpoint.hbase.schema.reader.xml;

import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaParseException;
import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaReader;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class XmlHbaseSchemaReader implements HbaseSchemaReader {

    public static final String DEFAULT_HBASE_SCHEMA_PATH = "classpath:hbase-schema/hbase-schema.xml";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ResourceLoader resourceLoader = new FileSystemResourceLoader();
    private final XmlHbaseSchemaParser xmlHbaseSchemaParser = new XmlHbaseSchemaParser();

    /**
     * Loads change sets from the hbase schema xml file from the default path - {@value DEFAULT_HBASE_SCHEMA_PATH}.
     *
     * @return list of change sets loaded
     * @throws HbaseSchemaParseException if there was a problem reading or parsing from the schema xml file
     */
    @Override
    public List<ChangeSet> loadChangeSets() {
        return loadChangeSets(DEFAULT_HBASE_SCHEMA_PATH);
    }

    /**
     * Loads change sets from the hbase schema xml file at the given path.
     *
     * @param path path to hbase schema xml file
     * @return list of change sets loaded
     * @throws HbaseSchemaParseException if there was a problem reading or parsing from the schema xml file
     */
    @Override
    public List<ChangeSet> loadChangeSets(String path) {
        Resource resource = resourceLoader.getResource(path);
        XmlParseContext xmlParseContext = new XmlParseContext(resource);
        try {
            loadChangeSets(xmlParseContext);
        } catch (HbaseSchemaParseException e) {
            logger.error("Error loading change sets from {}", xmlParseContext.getResource(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Error loading change sets from {}", xmlParseContext.getResource(), e);
            throw new HbaseSchemaParseException("Error loading change sets from " + xmlParseContext.getResource(), e);
        }
        return xmlParseContext.getChangeSets();
    }

    private void loadChangeSets(XmlParseContext xmlParseContext) throws IOException {
        Resource resource = xmlParseContext.getResource();
        XmlHbaseSchemaParseResult parseResult = readHbaseSchema(resource);
        for (String includeFile : parseResult.getIncludeFiles()) {
            Resource includeResource = createResource(resource, includeFile);
            xmlParseContext.setResource(includeResource);
            loadChangeSets(xmlParseContext);
        }
        xmlParseContext.addChangeSets(parseResult.getChangeSets());
    }

    private Resource createResource(Resource currentResource, String path) throws IOException {
        if (isAbsolutePath(path)) {
            return resourceLoader.getResource(path);
        }
        return currentResource.createRelative(path);
    }

    private XmlHbaseSchemaParseResult readHbaseSchema(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            InputSource inputSource = new InputSource(inputStream);
            return xmlHbaseSchemaParser.parseSchema(inputSource);
        }
    }

    private boolean isAbsolutePath(String path) {
        if (path.startsWith("/") || path.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            return true;
        }
        try {
            return ResourceUtils.toURI(path).isAbsolute();
        } catch (URISyntaxException e) {
            // conversion failed, assume relative
            return false;
        }
    }

}
