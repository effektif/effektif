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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/**
 * @author Tom Baeyens
 */
public class BaseInstanceImpl {

  public ScopeInstanceImpl parent;
  public WorkflowInstanceImpl workflowInstance;
  public WorkflowImpl workflow;
  public Configuration configuration;

  public BaseInstanceImpl() {
  }

  public BaseInstanceImpl(ScopeInstanceImpl parent) {
    this.parent = parent;
    this.workflowInstance = parent.workflowInstance;
    this.workflow = parent.workflow;
    this.configuration = parent.configuration;
  }

  public Configuration getConfiguration() {
    return configuration;
  }
  
  public ScopeInstanceImpl getParent() {
    return parent;
  }
  
  public WorkflowInstanceImpl getWorkflowInstance() {
    return workflowInstance;
  }
  
  public WorkflowImpl getWorkflow() {
    return workflow;
  }
}
