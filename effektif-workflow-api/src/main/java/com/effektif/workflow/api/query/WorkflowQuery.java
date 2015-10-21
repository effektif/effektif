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

import com.effektif.workflow.api.model.WorkflowId;



/**
 * A query for finding {@link com.effektif.workflow.api.workflow.ExecutableWorkflow} definitions
 * using {@link com.effektif.workflow.api.WorkflowEngine#findWorkflows(WorkflowQuery)}.
 *
 * @author Tom Baeyens
 */
public class WorkflowQuery extends Query {
  
  public static final String FIELD_CREATE_TIME = "createTime";
  
  protected String organizationId;
  protected WorkflowId workflowId;
  protected String workflowSource;
  
  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public WorkflowQuery organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  public WorkflowId getWorkflowId() {
    return this.workflowId;
  }
  public void setWorkflowId(WorkflowId workflowId) {
    this.workflowId = workflowId;
  }
  public WorkflowQuery workflowId(WorkflowId workflowId) {
    this.workflowId = workflowId;
    return this;
  }
  
  public String getWorkflowSource() {
    return this.workflowSource;
  }
  public void setWorkflowSource(String workflowSource) {
    this.workflowSource = workflowSource;
  }
  public WorkflowQuery workflowSource(String workflowSource) {
    this.workflowSource = workflowSource;
    return this;
  }

  public WorkflowQuery orderByCreateTime(OrderDirection direction) {
    orderBy(FIELD_CREATE_TIME, direction);
    return this;
  }

  @Override
  public WorkflowQuery skip(Integer skip) {
    super.skip(skip);
    return this;
  }
  @Override
  public WorkflowQuery limit(Integer limit) {
    super.limit(limit);
    return this;
  }
  @Override
  public WorkflowQuery orderBy(String field, OrderDirection direction) {
    super.orderBy(field, direction);
    return this;
  }
  
  
}
