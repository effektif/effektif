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
package com.effektif.workflow.impl.adapter;

import java.util.List;

import com.effektif.workflow.api.activities.AdapterActivity;
import com.effektif.workflow.api.types.ObjectType;


public class Adapter {

  // configuration fields
  protected String id;
  protected String url;
  protected String authorization;
  protected String organizationId;
  protected ObjectType objectType;

  // runtime status fields
  protected AdapterStatus status;
  protected List<AdapterActivity> recentActivity;
  protected Long executions;
  
  public String getUrl() {
    return this.url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public Adapter url(String url) {
    this.url = url;
    return this;
  }
  
  public String getAuthorization() {
    return this.authorization;
  }
  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }
  public Adapter authorization(String authorization) {
    this.authorization = authorization;
    return this;
  }

  public String getOrganizationId() {
    return this.organizationId;
  }
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  public Adapter organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  public AdapterStatus getStatus() {
    return this.status;
  }
  public void setStatus(AdapterStatus status) {
    this.status = status;
  }

  public String getId() {
    return this.id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Adapter id(String id) {
    this.id = id;
    return this;
  }

  public ObjectType getObjectType() {
    return this.objectType;
  }
  public void setObjectType(ObjectType objectType) {
    this.objectType = objectType;
  }
  
  public List<AdapterActivity> getRecentActivity() {
    return recentActivity;
  }
  
  public void setRecentActivity(List<AdapterActivity> recentActivity) {
    this.recentActivity = recentActivity;
  }
  
  public Long getExecutions() {
    return executions;
  }
  
  public void setExecutions(Long executions) {
    this.executions = executions;
  }
}
