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
package com.effektif.workflow.test.implementation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.json.RestJsonService;


/**
 * @author Tom Baeyens
 */
public class JsonTest {
  
  @Test
  public void testWorkflowJson() {
    RestJsonService restJsonService = new RestJsonService();
    restJsonService.registerBaseClass(Activity.class);
    restJsonService.registerSubClass(StartEvent.class);
    
    Workflow workflow = new Workflow()
      .id(new WorkflowId("i"))
      .activity(new StartEvent()
        .id("s")
      );
    
    String jsonString = restJsonService
      .createJsonWriter()
      .toString(workflow);
    
    System.out.println(jsonString);
    
    workflow = restJsonService
      .createJsonReader()
      .toObject(jsonString, Workflow.class);
    
    assertEquals("i", workflow.getId().getInternal());
    assertEquals(StartEvent.class, workflow.getActivities().get(0).getClass());
    assertEquals("s", workflow.getActivities().get(0).getId());
  }

}
