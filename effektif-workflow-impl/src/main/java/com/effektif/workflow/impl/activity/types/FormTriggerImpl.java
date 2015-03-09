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

import com.effektif.workflow.api.triggers.FormTrigger;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractTriggerImpl;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/**
 * @author Tom Baeyens
 */
public class FormTriggerImpl extends AbstractTriggerImpl<FormTrigger> {

  public Map<String,BindingImpl> bindings;
  
  public FormTriggerImpl() {
    super(FormTrigger.class);
  }

  @Override
  public void parse(WorkflowImpl workflow, FormTrigger formTrigger, WorkflowParser parser) {
    super.parse(workflow, formTrigger, parser);
    
    bindings = parser.parseForm(formTrigger.getForm());
  }
}
