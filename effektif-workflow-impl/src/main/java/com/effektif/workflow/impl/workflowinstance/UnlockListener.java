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
package com.effektif.workflow.impl.workflowinstance;


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
public interface UnlockListener {

  void unlocked(WorkflowInstanceImpl workflowInstanceImpl);

}
