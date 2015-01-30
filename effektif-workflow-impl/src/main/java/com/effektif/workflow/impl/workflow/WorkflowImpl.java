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

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;


public class WorkflowImpl extends ScopeImpl {
  
  public List<ActivityImpl> startActivities;
  public String organizationId;
  public String name;
  public LocalDateTime deployedTime;
  public String deployedBy;
  public Long version;
  public WorkflowEngineImpl workflowEngine;
  
  public WorkflowImpl() {
  }

  public void parse(Workflow apiWorkflow, WorkflowParser parser) {
    this.workflow = this;
    super.parse(apiWorkflow, parser, null);
    this.startActivities = parser.getStartActivities(this);
    this.organizationId = apiWorkflow.getOrganizationId();
    this.name = apiWorkflow.getName();
    this.deployedTime = apiWorkflow.getDeployedTime();
    this.deployedBy = apiWorkflow.getDeployedBy();
    this.version = apiWorkflow.getVersion();
    this.workflowEngine = configuration.get(WorkflowEngineImpl.class);
  }

  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }

//  public Workflow toWorkflow() {
//    Workflow workflow = new Workflow();
//    workflow.setOrganizationId(organizationId);
//    workflow.setName(name);
//    workflow.setDeployedTime(deployedTime);
//    workflow.setDeployedBy(deployedBy);
//    workflow.setVersion(version);
//    super.serialize(workflow);
//    return workflow;
//  }
}
