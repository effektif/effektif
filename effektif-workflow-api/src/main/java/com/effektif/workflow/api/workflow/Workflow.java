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
package com.effektif.workflow.api.workflow;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.ref.UserReference;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.xml.XmlElement;


/** An executable workflow in API format to deploy 
 * it into the {@link WorkflowEngine}.
 * 
 * From this API format, the workflow can be converted 
 * to following other formats:
 * <ul>
 *   <li>JSON java format (maps and lists)</li>
 *   <li>JSON format (String or stream)</li>
 *   <li>BPMN XML</li>
 *   <li>DB format</li>
 * </ul>
 * */
public class Workflow extends Scope {

  protected String source;
  protected LocalDateTime deployedTime;
  protected UserReference deployedBy;

  protected Trigger trigger;
  protected XmlElement bpmnDefinitions;
  
  protected AccessControlList acl;
  protected String organizationId;

  /** refers to the authoring form of this workflow.
   * @see #source(String) */
  public String getSource() {
    return this.source;
  }
  
  /** refers to the authoring form of this workflow.
   * @see #source(String) */
  public void setSource(String source) {
    this.source = source;
  }

  /** refers to the authoring form of this workflow.
   * This field is important if you want to deploy new versions of a 
   * workflow and start workflow instances in the latest version.
   * Eg this can reference the BPMN file.  Such a file evolves and 
   * can be deployed multiple times.  Each time a workflow is published 
   * with the same source, that means it's a new version. 
   * @see #getVersion() */
  public Workflow source(String source) {
    this.source = source;
    return this;
  }
  
  public LocalDateTime getDeployedTime() {
    return this.deployedTime;
  }
  public void setDeployedTime(LocalDateTime deployedTime) {
    this.deployedTime = deployedTime;
  }
  public Workflow deployedTime(LocalDateTime deployedTime) {
    this.deployedTime = deployedTime;
    return this;
  }
  
  public UserReference getDeployedBy() {
    return this.deployedBy;
  }
  public void setDeployedBy(UserReference deployedBy) {
    this.deployedBy = deployedBy;
  }
  public Workflow deployedBy(UserReference deployedBy) {
    this.deployedBy = deployedBy;
    return this;
  }
  
  public Trigger getTrigger() {
    return this.trigger;
  }
  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }
  public Workflow trigger(Trigger trigger) {
    this.trigger = trigger;
    return this;
  }
  
  /** stores the non-parsed BPMN portion of the definitions element */
  public XmlElement getBpmnDefinitions() {
    return this.bpmnDefinitions;
  }
  /** stores the non-parsed BPMN portion of the definitions element */
  public void setBpmnDefinitions(XmlElement bpmnDefinitions) {
    this.bpmnDefinitions = bpmnDefinitions;
  }
  
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public AccessControlList getAcl() {
    return this.acl;
  }
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public void setAcl(AccessControlList acl) {
    this.acl = acl;
  }
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public Workflow acl(AccessControlList acl) {
    this.acl = acl;
    return this;
  }

  /** optional organization (aka tenant or workspace) identification */
  public String getOrganizationId() {
    return this.organizationId;
  }
  /** optional organization (aka tenant or workspace) identification */
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  /** optional organization (aka tenant or workspace) identification */
  public Workflow organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  /** add an activity to the workflow */
  @Override
  public Workflow activity(Activity activity) {
    super.activity(activity);
    return this;
  }
  /** add an activity to the workflow */
  @Override
  public Workflow activity(String id, Activity activity) {
    super.activity(id, activity);
    return this;
  }
  /** add a transition to this workflow where the id is specified in the transition */
  @Override
  public Workflow transition(Transition transition) {
    super.transition(transition);
    return this;
  }
  /** add a transition to this workflow and set the given id. */
  @Override
  public Workflow transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }
  /** add a variable to this workflow and set the given id. */
  @Override
  public Workflow variable(String id, Type type) {
    super.variable(id, type);
    return this;
  }
  /** add a timer to this workflow. */
  @Override
  public Workflow timer(Timer timer) {
    super.timer(timer);
    return this;
  }
  /** sets the id of this workflow.
   * The id is not really used during execution. */
  @Override
  public Workflow id(String id) {
    super.id(id);
    return this;
  }
  
  @Override
  public Workflow name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public Workflow description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public Workflow property(String key, Object value) {
    super.property(key, value);
    return this;
  }
  
  @Override
  public Workflow propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

}
