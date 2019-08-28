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

package com.navercorp.pinpoint.hbase.manager;

import com.navercorp.pinpoint.hbase.manager.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author HyunGil Jeong
 */
@Component
public class XmlFormatter {

    private final Logger logger = LoggerFactory.getLogger(XmlFormatter.class);

    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public String formatXml(String xmlString) {
        if (StringUtils.isEmpty(xmlString)) {
            return "";
        }
        Document doc = createDocument(xmlString);
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new StringWriter());

        Transformer transformer = createTransformer();
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            logger.error(Markers.APP_LOG, "Error transforming {}", xmlString, e);
            throw new IllegalStateException(e.getMessage());
        }
        return result.getWriter().toString();
    }

    private Document createDocument(String xmlString) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(new StringReader((xmlString))));
        } catch (ParserConfigurationException e) {
            logger.error(Markers.APP_LOG, "Error creating document builder.", e);
            throw new IllegalStateException(e.getMessage());
        } catch (SAXException e) {
            logger.error(Markers.APP_LOG, "Error parsing xml value.", e);
            throw new IllegalStateException(e.getMessage());
        } catch (IOException e) {
            logger.error(Markers.APP_LOG, "IO error parsing xml value.", e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    private Transformer createTransformer() {
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            return transformer;
        } catch (TransformerConfigurationException e) {
            logger.error(Markers.APP_LOG, "Error creating transformer.", e);
            throw new IllegalStateException(e.getMessage());
        }
    }
}
