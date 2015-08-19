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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.BeforeClass;
import org.junit.Test;

import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.types.EmailAddressType;
import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.json.DefaultJsonStreamMapper;
import com.effektif.workflow.impl.json.JsonStreamMapper;


/**
 * Tests workflow instance serialisation to JSON, by serialising and deserialising workflow instance objects.
 *
 * @author Tom Baeyens
 */
public class WorkflowInstanceStreamTest {

  static JsonStreamMapper jsonStreamMapper = null;
  
  @BeforeClass
  public static void initialize() {
    jsonStreamMapper = new DefaultJsonStreamMapper();
    jsonStreamMapper.pretty();
  }

  public <T> T serialize(T o) {
    String jsonString = jsonStreamMapper.write(o);
    System.out.println(jsonString);
    return (T) jsonStreamMapper.readString(jsonString, o.getClass());
  }
  
  protected String getWorkflowInstanceIdInternal() {
    return "wiid";
  }

  @Test 
  public void testWorkflowInstance() {
    LocalDateTime now = new LocalDateTime();

    String workflowInstanceIdInternal = getWorkflowInstanceIdInternal();
    
    VariableInstance variableInstance = new VariableInstance();
    variableInstance.setId("v");
    variableInstance.setVariableId("vid");

    List<VariableInstance> variableInstances = new ArrayList<>();
    variableInstances.add(variableInstance);
    
    WorkflowInstance workflowInstance = new WorkflowInstance();
    workflowInstance.setId(new WorkflowInstanceId(workflowInstanceIdInternal));
    workflowInstance.setVariableInstances(variableInstances );
    workflowInstance.setStart(now);
    workflowInstance.setEnd(now);
    
    workflowInstance = serialize(workflowInstance);
    
    assertNotNull(workflowInstance);
    assertEquals(now, workflowInstance.getStart());
    assertEquals(now, workflowInstance.getEnd());
    variableInstance = workflowInstance.getVariableInstances().get(0);
    assertEquals("v", variableInstance.getId());
    assertEquals("vid", variableInstance.getVariableId());
  }

  @Test 
  public void testVariableInstanceString() {
    VariableInstance v = serializeVariableInstance("hello");
    assertEquals("hello", v.getValue());
  }

  @Test 
  public void testVariableInstanceDate() {
    LocalDateTime now = new LocalDateTime();
    VariableInstance v = serializeVariableInstance(now);
    assertEquals(now, v.getValue());
  }

  @Test 
  public void testVariableInstanceEmailAddress() {
    VariableInstance v = serializeVariableInstance("a@b.c", EmailAddressType.INSTANCE);
    assertEquals("a@b.c", v.getValue());
    assertEquals(EmailAddressType.class, v.getType().getClass());
  }

  protected VariableInstance serializeVariableInstance(Object value) {
    return serializeVariableInstance(value, null);
  }

  protected VariableInstance serializeVariableInstance(Object value, DataType dataType) {
    VariableInstance variableInstance = new VariableInstance();
    variableInstance.setValue(value);
    variableInstance.setType(dataType);
    
    List<VariableInstance> variableInstances = new ArrayList<>();
    variableInstances.add(variableInstance);
    
    WorkflowInstance workflowInstance = new WorkflowInstance();
    workflowInstance.setVariableInstances(variableInstances );
    
    workflowInstance = serialize(workflowInstance);
    
    return workflowInstance.getVariableInstances().get(0);
  }
}
