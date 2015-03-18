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
package com.effektif.workflow.impl.activity.types;

import java.util.Map;

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormInstance;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.triggers.FormTrigger;
import com.effektif.workflow.impl.FormBindings;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractTriggerImpl;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.workflow.WorkflowImpl;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class FormTriggerImpl extends AbstractTriggerImpl<FormTrigger> {

  public FormBindings formBindings;
  public JsonService jsonService;
  
  public FormTriggerImpl() {
    super(FormTrigger.class);
  }

  @Override
  public void parse(WorkflowImpl workflow, FormTrigger formTrigger, WorkflowParser parser) {
    super.parse(workflow, formTrigger, parser);
    this.jsonService = parser.getConfiguration(JsonService.class);
    Form form = formTrigger.getForm();
    if (form!=null) {
      formBindings = new FormBindings();
      parser.pushContext("form", formTrigger, formBindings, null);
      formBindings.parse(form, parser);
      parser.popContext();
    } else {
      parser.addWarning("Form trigger doesn't have a form");
    }
  }

  @Override
  public void applyTriggerData(WorkflowInstanceImpl workflowInstance, TriggerInstance triggerInstance) {
    FormInstance formInstance = (FormInstance) triggerInstance.getData(FormTrigger.FORM_INSTANCE_KEY);
    if (formBindings!=null) {
      formBindings.applyFormInstanceData(formInstance, workflowInstance);
    }
  }

  @Override
  public void deserializeTriggerInstance(TriggerInstance triggerInstance, WorkflowImpl workflow) {
    Object serializedFormInstance = triggerInstance.getData(FormTrigger.FORM_INSTANCE_KEY);
    if (serializedFormInstance instanceof Map) {
      FormInstance formInstance = jsonService.jsonMapToObject((Map)serializedFormInstance, FormInstance.class);
      triggerInstance.data(FormTrigger.FORM_INSTANCE_KEY, formInstance);
      formBindings.deserializeFormInstance(formInstance);
    }
  }
}
