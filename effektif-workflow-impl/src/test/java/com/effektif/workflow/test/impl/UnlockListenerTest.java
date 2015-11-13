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
package com.effektif.workflow.test.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;
import com.effektif.workflow.impl.workflowinstance.UnlockListener;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;
import com.effektif.workflow.test.WorkflowTest;


/** UnlockListener is a feature to notify external services after 
 * the workflow instance has been unlocked.
 * 
 * Wait states will pause the execution till an external 
 * message comes in that resumes execution of the workflow instance.
 * When that message arrives, the workflow instance has to be locked.
 * When the external work is automatic (not a user task) you may want 
 * to push a notification to the external service notifying 
 * a new activity instance has to be performed. 
 * If that notification is sent straight from inside the execute 
 * method of the wait state activity, the external service might 
 * send the message before the workflow instance is unlocked.
 * This would cause a lock exception and retry.  
 *    
 * @author Tom Baeyens
 */
public class UnlockListenerTest extends WorkflowTest {

  static List<String> notificationsReceived = null; // initialized in resetNotificationsReceived()

  public static class Listener implements UnlockListener {
    ActivityInstanceImpl activityInstance;
    public Listener(ActivityInstanceImpl activityInstance) {
      this.activityInstance = activityInstance;
    }
    @Override
    public void unlocked(WorkflowInstanceImpl workflowInstanceImpl) {
      // When the notification is received, we now check 
      // if the workflow instance store actually is unlocked
      WorkflowInstanceId workflowInstanceId = workflowInstanceImpl.getId();
      WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery().workflowInstanceId(workflowInstanceId);
      WorkflowInstanceImpl storedWorkflowInstance = cachedConfiguration
        .get(WorkflowInstanceStore.class)
        .findWorkflowInstances(workflowInstanceQuery)
        .get(0);
      assertNull(storedWorkflowInstance.lock);

      String notificationMessage = "Activity "+activityInstance.activity.id+" is started "+
                                   "and it's workflow instance unlocked";
      notificationsReceived.add(notificationMessage);
    }
  }
  
  @TypeName("unlockListening")
  public static class ListeningActivity extends Activity {
  }
  
  public static class ListeningActivityImpl extends AbstractActivityType {
    public ListeningActivityImpl() {
      super(ListeningActivity.class);
    }

    @Override
    public void execute(ActivityInstanceImpl activityInstance) {
      // Notifications should only be sent out after the workflow instance 
      // is unlocked.
      activityInstance.workflowInstance.addUnlockListener(new Listener(activityInstance));
      // ListeningActivityImpl is typically a wait state
    }
  } 
  
  @Before
  public void resetNotificationsReceived() {
    notificationsReceived = new ArrayList<>();
  }
  
  @Test
  public void testUnlockListener() {
    // Create a workflow
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("a", new ListeningActivity());
    
    // Deploy the workflow to the engine
    WorkflowId workflowId = workflowEngine
      .deployWorkflow(workflow)
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();

    // Start a new workflow instance
    workflowEngine
      .start(new TriggerInstance()
        .workflowId(workflowId));
    
    assertEquals("Activity a is started and it's workflow instance unlocked", notificationsReceived.get(0));
  }
}
