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
package com.effektif.workflow.api.model;

import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The runtime representation of a {@link com.effektif.workflow.api.workflow.Trigger}
 * when executing a workflow.
 *
 * @author Tom Baeyens
 */
public class TriggerInstance extends DataContainer {
  
  protected WorkflowInstanceId workflowInstanceId;
  protected WorkflowId workflowId;
  protected String sourceWorkflowId;
  protected List<String> startActivityIds;
  protected String businessKey;
  protected WorkflowInstanceId callingWorkflowInstanceId;
  protected String callingActivityInstanceId;

  public WorkflowId getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(WorkflowId workflowId) {
    this.workflowId = workflowId;
  }

  /** use this specific workflow version. 
   * If you want to use the latest version of a certain workflow, use {@link #sourceWorkflowId(String)}. */
  public TriggerInstance workflowId(WorkflowId workflowId) {
    this.workflowId = workflowId;
    return this;
  }

  public List<String> getStartActivityIds() {
    return startActivityIds;
  }

  public void setStartActivityIds(List<String> startActivityIds) {
    this.startActivityIds = startActivityIds;
  }

  /***
   * When there are multiple start events, starting all of them at the same time might not be desirable.
   * To only start one (or multiple) use addStartActivityId once (or multiple times with different activityIds).
   * If addStartActivityId is not called, all startEvents will be started/triggered.
   * @param startActivityId
   * @return TriggerInstance object, for linking several calls...
   */
  public TriggerInstance addStartActivityId(String startActivityId) {
    if(this.startActivityIds == null) this.startActivityIds = new ArrayList<>();
    this.startActivityIds.add(startActivityId);
    return this;
  }

  public String getSourceWorkflowId() {
    return this.sourceWorkflowId;
  }
  public void setSourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
  }
  
  /** use the lastest version of the workflow for the given source.
   * @see ExecutableWorkflow#sourceWorkflowId(String) */
  public TriggerInstance sourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
    return this;
  }

  public String getBusinessKey() {
    return this.businessKey;
  }
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  /**
   * Optional user-defined unique ID (unique in the scope of one workflow).
   */
  public TriggerInstance businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  /**
   * Optional ID that is passed.
   * Please be aware that worklfowInstanceIds are normally assigned by 
   * the (persistence implementation in the) engine.
   * You can provide your own here but please be aware that it 
   * might have to be compatible with the persistence implementation.
   */
  public WorkflowInstanceId getWorkflowInstanceId() {
    return this.workflowInstanceId;
  }
  /** pass in an id for the workflow instance that will be created,
   * ensure the id internal value is unique and that the configured 
   * persistence supports it. */
  public void setWorkflowInstanceId(WorkflowInstanceId workflowInstanceId) {
    this.workflowInstanceId = workflowInstanceId;
  }

  /** used by the call activity to establish the link between the calling activity instance 
   * and the called workflow instance */
  public WorkflowInstanceId getCallingWorkflowInstanceId() {
    return callingWorkflowInstanceId;
  }
  
  /** used by the call activity to establish the link between the calling activity instance 
   * and the called workflow instance */
  public void setCallingWorkflowInstanceId(WorkflowInstanceId callingWorkflowInstanceId) {
    this.callingWorkflowInstanceId = callingWorkflowInstanceId;
  }
  
  /** used by the call activity to establish the link between the calling activity instance 
   * and the called workflow instance */
  public String getCallingActivityInstanceId() {
    return callingActivityInstanceId;
  }

  /** used by the call activity to establish the link between the calling activity instance 
   * and the called workflow instance */
  public void setCallingActivityInstanceId(String callingActivityInstanceId) {
    this.callingActivityInstanceId = callingActivityInstanceId;
  }
  
  @Override
  public TriggerInstance data(String key, Object value) {
    super.data(key, value);
    return this;
  }
  
  @Override
  public TriggerInstance data(String key, Object value, DataType dataType) {
    super.data(key, value, dataType);
    return this;
  }
  
  @Override
  public TriggerInstance typedValue(String key, TypedValue value) {
    super.typedValue(key, value);
    return this;
  }
  
  @Override
  public TriggerInstance data(Map<String, Object> data) {
    super.data(data);
    return this;
  }
}
