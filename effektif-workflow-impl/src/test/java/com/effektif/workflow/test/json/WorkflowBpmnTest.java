/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.test.json;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.bpmn.BpmnMapper;

/**
 * Tests workflow serialisation to BPMN, by running {@link WorkflowStreamTest} with a different serialisation.
 *
 * @author Peter Hilton
 */
public class WorkflowBpmnTest extends WorkflowStreamTest {

  private static final Logger log = LoggerFactory.getLogger(WorkflowBpmnTest.class);
  private static BpmnMapper bpmnMapper;
  
  @BeforeClass
  public static void initialize() {
    bpmnMapper = new BpmnMapper();
  }

  @Override
  public <T> T serialize(T o) {
    Workflow w = null;
    if (o instanceof Activity) {
      w = new Workflow().activity((Activity) o);
    } else {
      w = (Workflow) o;
    }

    String xmlString = bpmnMapper.writeToString(w);

    log.info("\n" + xmlString + "\n");

    validateBpmnXml(xmlString);

    w = bpmnMapper.readFromString(xmlString);

    if (o instanceof Activity) {
      return (T) w.getActivities().get(0);
    } else {
      return (T) w;
    }
  }

  /**
   * Performs XML schema validation on the generated XML using the BPMN 2.0 schema.
   */
  private void validateBpmnXml(String bpmnDocument) {
    String directory = WorkflowBpmnTest.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(5);
    File schemaFile = new File(directory, "bpmn/xsd/BPMN20.xsd");
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
  }}
