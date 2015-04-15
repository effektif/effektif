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
package com.effektif.workflow.test.serialization;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.triggers.FormTrigger;
import com.effektif.workflow.test.implementation.BpmnProcessTest;
import org.junit.BeforeClass;

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.mapper.BpmnMapper;
import org.xml.sax.SAXException;

/**
 * @author Tom Baeyens
 */
public class BpmnMapperTest extends AbstractMapperTest {

  static BpmnMapper bpmnMapper = new BpmnMapper();
  
  @BeforeClass
  public static void initialize() {
    initializeMappings();
    bpmnMapper = new BpmnMapper();
    bpmnMapper.setMappings(mappings);
  }
  
  @Override
  protected <T> T serialize(T o) {
    Workflow w = null;
    if (o instanceof Activity) {
      w = new Workflow()
      .activity((Activity)o);
    } else if (o instanceof Form) {
      w = new Workflow().activity(new UserTask().form((Form) o));
    } else {
      w = (Workflow) o;
    }

    String xmlString = bpmnMapper
      .writeToString(w);
    
    System.out.println(xmlString);
    System.out.println();

    validateBpmnXml(xmlString);

    w = bpmnMapper
      .readFromString(xmlString);
    
    if (o instanceof Activity) {
      return (T) w.getActivities().get(0);
    } else if (o instanceof Form) {
      return (T) ((UserTask) w.getActivities().get(0)).getForm();
    } else {
      return (T) w;
    }
  }

  /**
   * Performs XML schema validation on the generated XML using the BPMN 2.0 schema.
   */
  protected void validateBpmnXml(String bpmnDocument) {
    String directory = BpmnMapperTest.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(5);
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
  }
}
