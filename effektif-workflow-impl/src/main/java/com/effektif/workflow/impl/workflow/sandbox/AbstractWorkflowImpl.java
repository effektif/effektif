package com.effektif.workflow.impl.workflow.sandbox;

import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractTriggerImpl;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflow.ScopeImpl;

import java.util.List;

/**
 * @author mavo
 */
public abstract class AbstractWorkflowImpl extends ScopeImpl {

  public WorkflowId id;
  public String name;
  public WorkflowEngineImpl workflowEngine;
  public String sourceWorkflowId;
  public List<ActivityImpl> startActivities;
  public AbstractTriggerImpl trigger;
  public boolean enableCases;

  public abstract void parse(AbstractWorkflow workflow, WorkflowParser parser);
//  {
//    this.workflow = this;
//    this.name = workflow.getName();
//    super.parse(workflow, null, parser);
//    this.startActivities = parser.getStartActivities(this);
//    this.workflowEngine = configuration.get(WorkflowEngineImpl.class);
//    this.sourceWorkflowId = workflow.getSourceWorkflowId();
//    this.enableCases = workflow.isEnableCases();
//
//    Trigger triggerApi = workflow.getTrigger();
//    if (triggerApi!=null) {
//      ActivityTypeService activityTypeService = configuration.get(ActivityTypeService.class);
//      this.trigger = activityTypeService.instantiateTriggerType(triggerApi);
//      this.trigger.parse(this, triggerApi, parser);
//    }
//  }

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
