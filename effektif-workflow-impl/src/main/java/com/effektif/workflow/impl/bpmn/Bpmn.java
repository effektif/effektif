/*
 * Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.bpmn;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * Constants for fixed string values used by both the {@link BpmnReaderImpl} and {@link BpmnWriterImpl}.
 */
public class Bpmn {

  public static final String BPMN_URI = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  public static final String BPMN_DI_URI = "http://www.omg.org/spec/BPMN/20100524/DI";
  public static final String EFFEKTIF_URI = "http://effektif.com/bpmn20";

  public static final String OMG_DI_URI = "http://www.omg.org/spec/DD/20100524/DI";
  public static final String OMG_DC_URI = "http://www.omg.org/spec/DD/20100524/DC";

  public static final String KEY_BPMN = "bpmn";
  public static final String KEY_DEFINITIONS = "bpmnDefinitions";

  /**
   * Performs XML schema validation on the given XML using the BPMN 2.0 schema.
   */
  public static void validate(String bpmnDocument) {
    if (bpmnDocument == null) {
      throw new IllegalArgumentException("null bpmnDocument");
    }
    String directory = Bpmn.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(5);
    File schemaFile = new File(new File(directory).getParent(), "classes/xsd/BPMN20.xsd");
    Source xml = new StreamSource(new StringReader(bpmnDocument));
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    try {
      Schema schema = schemaFactory.newSchema(schemaFile);
      Validator validator = schema.newValidator();
      validator.validate(xml);
    } catch (SAXException e) {
      throw new RuntimeException("BPMN XML validation error: " + e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException("IOException during BPMN XML validation: " + e.getMessage());
    }
  }
}
