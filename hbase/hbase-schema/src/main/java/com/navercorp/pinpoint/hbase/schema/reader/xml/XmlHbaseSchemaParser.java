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

import com.navercorp.pinpoint.hbase.schema.definition.xml.HbaseSchema;
import com.navercorp.pinpoint.hbase.schema.reader.HbaseSchemaParseException;
import com.navercorp.pinpoint.hbase.schema.reader.xml.mapper.HbaseSchemaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author HyunGil Jeong
 */
class XmlHbaseSchemaParser {

    private static final String SCHEMA_VERSION = "1.0";
    private static final String XSD_FILE = "pinpoint-hbase-" + SCHEMA_VERSION + ".xsd";

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlHbaseSchemaParser.class);

    private static final JAXBContext JAXB_CONTEXT = createJaxbContext();
    private static final Schema SCHEMA = createSchema();

    private final HbaseSchemaMapper mapper = new HbaseSchemaMapper(JAXB_CONTEXT, SCHEMA);

    private static JAXBContext createJaxbContext() {
        try {
            return JAXBContext.newInstance(HbaseSchema.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to initialize parser", e);
        }
    }

    private static Schema createSchema() {
        try (InputStream xsdInputStream = XmlHbaseSchemaParser.class.getResourceAsStream(XSD_FILE)) {
            if (xsdInputStream == null) {
                LOGGER.warn("Unabled to find: {}, skipping validation", XSD_FILE);
                return null;
            }
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                return schemaFactory.newSchema(new StreamSource(xsdInputStream));
            } catch (SAXException e) {
                LOGGER.warn("Unable to parse: {}, skipping validation", XSD_FILE);
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("{} file I/O error, skipping validation. Error:{}", XSD_FILE, e.getMessage(), e);
            return null;
        }
    }

    XmlHbaseSchemaParseResult parseSchema(InputSource inputSource) {
        try {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            unmarshaller.setSchema(SCHEMA);
            HbaseSchema hbaseSchema = (HbaseSchema) unmarshaller.unmarshal(inputSource);
            return mapper.map(hbaseSchema);
        } catch (JAXBException e) {
            Throwable linkedException = e.getLinkedException();
            if (linkedException == null) {
                throw new IllegalStateException("JAXB error", e);
            }
            throw new HbaseSchemaParseException(linkedException.getMessage(), linkedException);
        }
    }
}
