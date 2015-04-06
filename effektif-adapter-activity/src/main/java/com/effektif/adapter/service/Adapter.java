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
package com.effektif.adapter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.impl.activity.ActivityDescriptor;
import com.effektif.workflow.impl.data.source.DataSourceDescriptor;


/**
 * @author Tom Baeyens
 */
public class Adapter {

  // configuration fields
  protected String id;
  protected String url;
  protected String authorization;
  protected String organizationId;
  protected Map<String,ActivityDescriptor> activityDescriptors;
  protected Map<String,DataSourceDescriptor> dataSourceDescriptors;

  // runtime status fields
  protected AdapterStatus status;
  protected List<AdapterLog> logs;
  protected Map<String,Long> executions;
  
  public void setActivityDescriptors(AdapterDescriptors adapterDescriptors) {
    if (adapterDescriptors!=null) {
      if (adapterDescriptors.getActivityDescriptors()!=null) {
        for (ActivityDescriptor activityDescriptor: adapterDescriptors.getActivityDescriptors()) {
          if (activityDescriptors == null) {
            activityDescriptors = new HashMap<>();
          }
          activityDescriptors.put(activityDescriptor.getActivityKey(), activityDescriptor);
        }
      }
      if (adapterDescriptors.getDataSourceDescriptors()!=null) {
        for (DataSourceDescriptor dataSourceDescriptor: adapterDescriptors.getDataSourceDescriptors()) {
          if (dataSourceDescriptors == null) {
            dataSourceDescriptors = new HashMap<>();
          }
          dataSourceDescriptors.put(dataSourceDescriptor.getDataSourceKey(), dataSourceDescriptor);
        }
      }
    }
  }
  
  public ActivityDescriptor getActivityDescriptor(String activityKey) {
    return activityDescriptors!=null ? activityDescriptors.get(activityKey) : null;
  }

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

  public List<AdapterLog> getLogs() {
    return logs;
  }
  
  public void setLogs(List<AdapterLog> logs) {
    this.logs = logs;
  }

}
