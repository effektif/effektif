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

import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.mapper.BpmnMapper;


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
    } else {
      w = (Workflow) o;
    }
    
    String xmlString = bpmnMapper
      .createWriter()
      .toBpmnString(w);
    
    System.out.println(xmlString);
    System.out.println();

    w = bpmnMapper
      .createReader()
      .toWorkflow(xmlString);
    
    if (o instanceof Activity) {
      return (T) w.getActivities().get(0);
    } else {
      return (T) w;
    }
  }
}
