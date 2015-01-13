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
package com.effektif.workflow.api.workflow;

import org.joda.time.LocalDateTime;


public class Workflow extends Scope {

  protected String name;
  protected LocalDateTime deployedTime;
  protected String deployedBy;
  protected String organizationId;
  protected String processId;
  protected Long version;
  protected ParseIssues issues;

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Workflow name(String name) {
    this.name = name;
    return this;
  }

  public LocalDateTime getDeployedTime() {
    return this.deployedTime;
  }
  public void setDeployedTime(LocalDateTime deployedTime) {
    this.deployedTime = deployedTime;
  }

  public String getDeployedBy() {
    return this.deployedBy;
  }
  public void setDeployedBy(String deployedBy) {
    this.deployedBy = deployedBy;
  }
  public Workflow deployedBy(String deployedBy) {
    this.deployedBy = deployedBy;
    return this;
  }

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public Workflow organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public String getProcessId() {
    return this.processId;
  }
  public void setProcessId(String processId) {
    this.processId = processId;
  }
  public Workflow processId(String processId) {
    this.processId = processId;
    return this;
  }
  
  public Long getVersion() {
    return this.version;
  }
  public void setVersion(Long version) {
    this.version = version;
  }
  public Workflow version(Long version) {
    this.version = version;
    return this;
  }
  
  @Override
  public Workflow activity(Activity activity) {
    super.activity(activity);
    return this;
  }
  @Override
  public Workflow transition(Transition transition) {
    super.transition(transition);
    return this;
  }
  @Override
  public Workflow variable(Variable variable) {
    super.variable(variable);
    return this;
  }
  @Override
  public Workflow timer(Timer timer) {
    super.timer(timer);
    return this;
  }
  @Override
  public Workflow id(String id) {
    super.id(id);
    return this;
  }
  @Override
  public Workflow property(String key, Object value) {
    super.property(key, value);
    return this;
  }
  
  public ParseIssues getIssues() {
    return issues;
  }
  
  public void setIssues(ParseIssues issues) {
    this.issues = issues;
  }
}
