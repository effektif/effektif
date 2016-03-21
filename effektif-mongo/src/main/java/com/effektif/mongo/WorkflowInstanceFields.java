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
package com.effektif.mongo;


public interface WorkflowInstanceFields extends ScopeInstanceFields {

  String ORGANIZATION_ID = "organizationId";
  String WORKFLOW_ID = "workflowId";
  String ACTIVITY_INSTANCES = "activityInstances";
  String ARCHIVED_ACTIVITY_INSTANCES = "archivedActivities";
  String VARIABLE_INSTANCES = "variableInstances";
  String LOCK = "lock";
  String UPDATES = "updates";
  String WORK = "work";
  String WORK_ASYNC = "workAsync";
  String CALLING_WORKFLOW_INSTANCE_ID = "callingWorkflowInstanceId";
  String CALLING_ACTIVITY_INSTANCE_ID = "callingActivityInstanceId";
  String NEXT_ACTIVITY_INSTANCE_ID = "nextActivityInstanceId";
  String NEXT_VARIABLE_INSTANCE_ID = "nextVariableInstanceId";
  String JOBS = "jobs";
  String PROPERTIES = "properties";
  String BUSINESS_KEY = "businessKey";

  interface Lock {
    String TIME = "time";
    String OWNER = "owner";
  }
  
  interface VariableInstance {
    String VARIABLE_ID = "variableId";
    String VALUE = "value";
    String TYPE = "type";
  }
}