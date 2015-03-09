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

import org.junit.Test;

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.triggers.FormTrigger;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class FormTriggerTest extends WorkflowTest {
  
/*  

  { trigger: {
      form: {
        fields : [
          { id="1",
            type: text }
        ]
      },
      outputBindings : {
        "1" : "v1"
      }
    },
    variables : [ {
      id: "v1",
      type: text
    ]
  }
  
*/

  @Test
  public void testFormTrigger() {
//    Workflow workflow = new Workflow()
//      .trigger(new FormTrigger()
//        .form(new Form()));
//    
//    deploy(workflow);
//    
//    start(workflow);
//
  }
}
