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
package com.effektif.workflow.test.api;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.form.FormInstance;
import com.effektif.workflow.api.triggers.FormTrigger;
import com.effektif.workflow.api.types.DecisionType;
import com.effektif.workflow.api.types.NumberType;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class FormTriggerTest extends WorkflowTest {
  
  @Test
  public void testFormTriggerNameAndTypeResolving() {
    Workflow workflow = new Workflow()
      .variable(new Variable()
        .id("v1")
        .name("Veewan") // the field using this variable should pick up this name by default 
        .type(new TextType())) // the field using this variable should pick up this type
      .trigger(new FormTrigger()
        // by default, the variableId is also taken as the fieldId
        .field("v1"));
    
    
    // deploying will initialize the name and type on the form field 
    // because it is linked to variable v1
    deploy(workflow);
    
    workflow = workflowEngine.findWorkflows(null).get(0);
    FormTrigger formTrigger =  (FormTrigger) workflow.getTrigger();
    List<FormField> fields = formTrigger.getForm().getFields();
    assertNotNull(fields.get(0).getId());
    assertEquals("Veewan", fields.get(0).getName());
    assertEquals(TextType.class, fields.get(0).getType().getClass());
    
    WorkflowInstance workflowInstance = start(workflow, new FormInstance()
      .value("v1", "hello"));
    
    assertEquals("hello", workflowInstance.getVariableValue("v1"));
  }

  @Test
  public void testFormTriggerCustomFieldId() {
    Workflow workflow = new Workflow()
      .variable("v1", new NumberType())
      .trigger(new FormTrigger()
        .field(new FormField()
          .id("f1")  // users can also define their own field ids
          .binding("v1")));
    
    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow, new FormInstance()
      .value("f1", 5));
    
    Number value = (Number)workflowInstance.getVariableValue("v1");
    assertEquals(5, value.intValue()); // the value might be of any number type, but must be 5
  }

  @Test
  public void testFormTriggerDecision() {
    Workflow workflow = new Workflow()
      .variable("v1", new DecisionType()
        .option("Approve")
        .option("Reject"))
      .trigger(new FormTrigger()
        .field("v1"));
    
    deploy(workflow);

    workflow = workflowEngine.findWorkflows(null).get(0);
    FormTrigger formTrigger =  (FormTrigger) workflow.getTrigger();
    Form form = formTrigger.getForm();
    List<FormField> fields = form.getFields();
    FormField formField = fields.get(0);
    assertEquals("v1", formField.getId());
    assertEquals(DecisionType.class, formField.getType().getClass());
    DecisionType decisionType = (DecisionType) formField.getType();
    assertEquals("Approve", decisionType.getOptions().get(0).getId());
    assertEquals("Reject", decisionType.getOptions().get(1).getId());
    
    WorkflowInstance workflowInstance = start(workflow, new FormInstance()
      .value("v1", "Approve"));
    
    assertEquals("Approve", workflowInstance.getVariableValue("v1"));
  }
}
