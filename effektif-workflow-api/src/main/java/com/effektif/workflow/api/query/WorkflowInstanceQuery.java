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
package com.effektif.workflow.api.query;

import com.effektif.workflow.api.model.WorkflowInstanceId;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

/**
 * @author Tom Baeyens
 */
public class WorkflowInstanceQuery {

  public static final String FIELD_START = "start";

  protected WorkflowInstanceId workflowInstanceId;
  protected String activityId;
  protected Integer skip;
  protected Integer limit;
  protected List<OrderBy> orderBy;
  protected LocalDateTime lockedBefore;
  
  public WorkflowInstanceQuery workflowInstanceId(WorkflowInstanceId workflowInstanceId) {
    setWorkflowInstanceId(workflowInstanceId);
    return this;
  }
  
  public WorkflowInstanceId getWorkflowInstanceId() {
    return workflowInstanceId;
  }
  
  public void setWorkflowInstanceId(WorkflowInstanceId processInstanceId) {
    this.workflowInstanceId = processInstanceId;
  }


  public WorkflowInstanceQuery activityId(String activityId) {
    setActivityId(activityId);
    return this;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }


  public Integer getSkip() {
    return this.skip;
  }
  public void setSkip(Integer skip) {
    this.skip = skip;
  }
  public WorkflowInstanceQuery skip(Integer skip) {
    this.skip = skip;
    return this;
  }
  
  public Integer getLimit() {
    return this.limit;
  }
  public void setLimit(Integer limit) {
    this.limit = limit;
  }
  public WorkflowInstanceQuery limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public List<OrderBy> getOrderBy() {
    return orderBy;
  }
  
  public void setOrderBy(List<OrderBy> orderBy) {
    this.orderBy = orderBy;
  }
  
  public WorkflowInstanceQuery orderByStart(OrderDirection direction) {
    orderBy(FIELD_START, direction);
    return this;
  }
  
  public void orderBy(String field, OrderDirection direction) {
    if (orderBy==null) {
      orderBy = new ArrayList<>();
    }
    orderBy.add(new OrderBy()
      .field(field)
      .direction(direction));
  }

  public LocalDateTime getLockedBefore() {
    return lockedBefore;
  }
  public void setLockedBefore(LocalDateTime dateTime) {
    this.lockedBefore = dateTime;
  }
  public WorkflowInstanceQuery lockedBefore(LocalDateTime dateTime) {
    this.lockedBefore = dateTime;
    return this;
  }
}
