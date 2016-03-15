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

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.impl.bpmn.Bpmn;
import com.effektif.workflow.impl.bpmn.BpmnMapper;


/**
 * Tests workflow serialisation to BPMN, by running {@link WorkflowStreamTest} with a different serialisation.
 *
 * @author Peter Hilton
 */
public class BpmnTest extends WorkflowStreamTest {

  protected static final Logger log = LoggerFactory.getLogger(BpmnTest.class);
  static BpmnMapper bpmnMapper;
  
  @BeforeClass
  public static void initialize() {
    if (bpmnMapper==null) {
      bpmnMapper = BpmnMapper.createBpmnMapperForTest();
    }
  }
  
  public static BpmnMapper getBpmnMapper() {
    initialize();
    return bpmnMapper;
  }

  @Override
  public <T extends AbstractWorkflow> T serializeWorkflow(T workflow) {
    String xmlString = bpmnMapper
      .writeToString(workflow);
    
    log.info("\n" + xmlString + "\n");

    Bpmn.validate(xmlString);

    return (T) bpmnMapper
      .readFromString(xmlString);
  }
}
