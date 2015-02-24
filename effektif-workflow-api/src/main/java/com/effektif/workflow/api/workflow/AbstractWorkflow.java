/* Copyright (c) 2014, Effektif GmbH.
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

import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.xml.XmlElement;


/**
 * @author Tom Baeyens
 */
public class AbstractWorkflow extends Scope {

  protected Trigger trigger;
  protected XmlElement bpmnDefinitions;
  
  protected AccessControlList access;
  protected String organizationId;
  
  protected String caseNameTemplate;

  public String getSourceWorkflowId() {
    return id;
  }

  public Trigger getTrigger() {
    return this.trigger;
  }
  public void setTrigger(Trigger trigger) {
    this.trigger = trigger;
  }
  public AbstractWorkflow trigger(Trigger trigger) {
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
  public AccessControlList getAccess() {
    return this.access;
  }
  /** the access control list specifies which actions are permitted by whom.
   * If not specified, all is allowed. */
  public void setAccess(AccessControlList access) {
    this.access = access;
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
  public AbstractWorkflow organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  /** optional template to initialize the name of cases */
  public String getCaseNameTemplate() {
    return this.caseNameTemplate;
  }
  public void setCaseNameTemplate(String caseNameTemplate) {
    this.caseNameTemplate = caseNameTemplate;
  }
}
