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



/**
 * @author Tom Baeyens
 */
public class Message extends DataContainer {

  protected WorkflowInstanceId workflowInstanceId;
  protected String sourceWorkflowId;
  protected String businessKey;
  protected String activityInstanceId;

  public WorkflowInstanceId getWorkflowInstanceId() {
    return this.workflowInstanceId;
  }
  public void setWorkflowInstanceId(WorkflowInstanceId workflowInstanceId) {
    this.workflowInstanceId = workflowInstanceId;
  }
  public Message workflowInstanceId(WorkflowInstanceId workflowInstanceId) {
    this.workflowInstanceId = workflowInstanceId;
    return this;
  }
  
  public String getActivityInstanceId() {
    return this.activityInstanceId;
  }
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
  public Message activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public String getSourceWorkflowId() {
    return this.sourceWorkflowId;
  }
  public void setSourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
  }
  /** combination of a sourceWorkflowId and a businessKey  
   * is an alternative way to identifying a workflowinstance
   * as opposed to using {@link #workflowInstanceId(String) } */
  public Message sourceWorkflowId(String sourceWorkflowId) {
    this.sourceWorkflowId = sourceWorkflowId;
    return this;
  }
  
  public String getBusinessKey() {
    return this.businessKey;
  }
  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  /** combination of a sourceWorkflowId and a businessKey  
   * is an alternative way to identifying a workflowinstance
   * as opposed to using {@link #workflowInstanceId(String) } */
  public Message businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }
  
  @Override
  public Message data(String key, Object value) {
    super.data(key, value);
    return this;
  }
  
  @Override
  public Message data(String key, Object value, DataType dataType) {
    super.data(key, value, dataType);
    return this;
  }
  
  @Override
  public Message typedValue(String key, TypedValue value) {
    super.typedValue(key, value);
    return this;
  }

  @Override
  public Message transientDataOpt(String key, Object value) {
    return (Message) super.transientDataOpt(key, value);
  }
  @Override
  public Message transientData(String key, Object value) {
    return (Message) super.transientData(key, value);
  }
}