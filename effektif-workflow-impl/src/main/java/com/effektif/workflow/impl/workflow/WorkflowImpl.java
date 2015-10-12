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
package com.effektif.workflow.impl.workflow;

import java.util.List;

import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractTriggerImpl;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class WorkflowImpl extends ScopeImpl {
  
  public WorkflowId id;
  public String name;
  public WorkflowEngineImpl workflowEngine;
  public String sourceWorkflowId;
  public List<ActivityImpl> startActivities;
  public AbstractTriggerImpl trigger;
  public boolean enableCases;
  
  public WorkflowImpl() {
  }

  public void parse(AbstractWorkflow workflow, WorkflowParser parser) {
    this.workflow = this;
    this.name = workflow.getName();
    super.parse(workflow, null, parser);
    this.startActivities = parser.getStartActivities(this);
    this.workflowEngine = configuration.get(WorkflowEngineImpl.class);
    this.sourceWorkflowId = workflow.getSourceWorkflowId();
    this.enableCases = workflow.isEnableCases();
    
    Trigger triggerApi = workflow.getTrigger();
    if (triggerApi!=null) {
      ActivityTypeService activityTypeService = configuration.get(ActivityTypeService.class);
      this.trigger = activityTypeService.instantiateTriggerType(triggerApi);
      this.trigger.parse(this, triggerApi, parser);
    }
  }
  
  public String getIdText() {
    return id!=null ? id.getInternal() : "null";
  }

  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }
  
  public WorkflowEngineImpl getWorkflowEngine() {
    return workflowEngine;
  }

  public String getSourceWorkflowId() {
    return sourceWorkflowId;
  }

  public List<ActivityImpl> getStartActivities() {
    return startActivities;
  }
  
  public AbstractTriggerImpl getTrigger() {
    return trigger;
  }

  public WorkflowId getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
