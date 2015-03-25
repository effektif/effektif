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
package com.effektif.workflow.test.api;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.form.FormInstance;
import com.effektif.workflow.api.triggers.FormTrigger;
import com.effektif.workflow.api.types.ChoiceType;
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
        .id("version")
        .name("Version number") // the field using this variable should pick up this name by default
        .type(new TextType())) // the field using this variable should pick up this type
      .trigger(new FormTrigger()
        // by default, the variableId is also taken as the fieldId
        .field("version"));
    
    
    // deploying will initialize the name and type on the form field 
    // because it is linked to variable version
    deploy(workflow);
    
    workflow = workflowEngine.findWorkflows(null).get(0);
    FormTrigger formTrigger =  (FormTrigger) workflow.getTrigger();
    List<FormField> fields = formTrigger.getForm().getFields();
    assertNotNull(fields.get(0).getId());
    assertEquals("Version number", fields.get(0).getName());
    assertEquals(TextType.class, fields.get(0).getType().getClass());
    
    WorkflowInstance workflowInstance = start(workflow, new FormInstance()
      .value("version", "4.2"));
    
    assertEquals("4.2", workflowInstance.getVariableValue("version"));
  }

  @Test
  public void testFormTriggerCustomFieldId() {
    Workflow workflow = new Workflow()
      .variable("version", new NumberType())
      .trigger(new FormTrigger()
        .field(new FormField()
          .id("versionField")  // users can also define their own field ids
          .name("Version number")
          .bindingExpression("version")));
    
    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow,
      new FormInstance().value("versionField", 5));
    
    Number value = (Number)workflowInstance.getVariableValue("version");
    assertEquals(5, value.intValue()); // the value might be of any number type, but must be 5
  }

  @Test
  public void testFormTriggerDecision() {
    Workflow workflow = new Workflow()
      .variable("reviewResult", new ChoiceType()
        .option("Approve")
        .option("Reject"))
      .trigger(new FormTrigger()
        .field("reviewResult"));
    
    deploy(workflow);

    workflow = workflowEngine.findWorkflows(null).get(0);
    FormTrigger formTrigger =  (FormTrigger) workflow.getTrigger();
    Form form = formTrigger.getForm();
    List<FormField> fields = form.getFields();
    FormField formField = fields.get(0);
    assertEquals("reviewResult", formField.getId());
    assertEquals(ChoiceType.class, formField.getType().getClass());
    ChoiceType choiceType = (ChoiceType) formField.getType();
    assertEquals("Approve", choiceType.getOptions().get(0).getId());
    assertEquals("Reject", choiceType.getOptions().get(1).getId());
    
    WorkflowInstance workflowInstance = start(workflow, new FormInstance()
      .value("reviewResult", "Approve"));
    
    assertEquals("Approve", workflowInstance.getVariableValue("reviewResult"));
  }
}
