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


public interface JobFields {
  
    public String _ID = "_id";
    public String KEY = "key";
    public String DUEDATE = "duedate";
    public String LOCK = "lock";
    public String EXECUTIONS = "executions";
    public String RETRIES = "retries";
    public String RETRY_DELAY = "retryDelay";
    public String DONE = "done";
    public String DEAD = "dead";
    public String ORGANIZATION_ID = "organizationId";
    public String PROCESS_ID = "processId";
    public String WORKFLOW_ID = "workflowId";
    public String WORKFLOW_INSTANCE_ID = "workflowInstanceId";
    public String LOCK_WORKFLOW_INSTANCE = "lockWorkflowInstance";
    public String ACTIVITY_INSTANCE_ID = "activityInstanceId";
    public String ERROR = "error";
    public String LOGS = "logs";
    public String TIME = "time";
    public String DURATION = "duration";
    public String OWNER = "owner";
    public String JOB_TYPE = "jobType";
  }