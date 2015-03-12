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
import com.effektif.workflow.impl.email.MailTrigger;
import com.effektif.workflow.test.WorkflowTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Peter Hilton
 */
public class MailTriggerTest extends WorkflowTest {
  
  @Test
  public void testMailTriggerNameAndTypeResolving() {
    Workflow workflow = new Workflow()
      .trigger(new MailTrigger());
    
    deploy(workflow);
    
    workflow = workflowEngine.findWorkflows(null).get(0);
    MailTrigger trigger =  (MailTrigger) workflow.getTrigger();

    // TODO ???
  }
}
