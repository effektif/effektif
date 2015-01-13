/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.workflow;

import java.util.List;

import com.effektif.workflow.api.workflow.Workflow;


public class WorkflowImpl extends ScopeImpl {
  
  public List<ActivityImpl> startActivities;
  public String organizationId;
  public String name;
  public Long deployedTime;
  public String deployedBy;
  public Long version;

  public void parse(Workflow apiWorkflow, WorkflowParser parse) {
    this.workflow = this;
    super.parse(apiWorkflow, parse, null);
    this.startActivities = parse.getStartActivities(this);
    this.organizationId = apiWorkflow.getOrganizationId();
    this.name = apiWorkflow.getName();
    this.deployedTime = apiWorkflow.getDeployedTime();
    this.deployedBy = apiWorkflow.getDeployedBy();
    this.version = apiWorkflow.getVersion();
  }

  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }
  
}
