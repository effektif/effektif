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
package com.effektif.workflow.impl.workflowinstance;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.workflow.WorkflowImpl;


/**
 * @author Tom Baeyens
 */
public class BaseInstanceImpl {

  public ScopeInstanceImpl parent;
  public WorkflowInstanceImpl workflowInstance;
  public WorkflowImpl workflow;
  public Configuration configuration;
  public Map<String,Object> properties;
  public Map<String,Object> transientProperties;

  public BaseInstanceImpl() {
  }

  public BaseInstanceImpl(ScopeInstanceImpl parent) {
    this.parent = parent;
    this.workflowInstance = parent.workflowInstance;
    this.workflow = parent.workflow;
    this.configuration = parent.configuration;
  }

  public Configuration getConfiguration() {
    return configuration;
  }
  
  public ScopeInstanceImpl getParent() {
    return parent;
  }
  
  public WorkflowInstanceImpl getWorkflowInstance() {
    return workflowInstance;
  }
  
  public WorkflowImpl getWorkflow() {
    return workflow;
  }

  public Map<String,Object> getProperties() {
    return this.properties;
  }
  public void setProperties(Map<String,Object> properties) {
    this.properties = properties;
  }
  public Object getProperty(String key) {
    return properties!=null ? properties.get(key) : null;
  }
  public void setProperty(String key,Object value) {
    if (properties==null) {
      properties = new HashMap<>();
    }
    this.properties.put(key, value);
  }
  public void setPropertyOpt(String key,Object value) {
    if (value==null) {
      return;
    }
    setProperty(key, value);
  }
  public Object removeProperty(String key) {
    return properties!=null ? properties.remove(key) : null;
  }

  public Map<String,Object> getTransientProperties() {
    return this.transientProperties;
  }
  public void setTransientProperties(Map<String,Object> transientProperties) {
    this.transientProperties = transientProperties;
  }
  public Object getTransientProperty(String key) {
    return transientProperties!=null ? transientProperties.get(key) : null;
  }
  public void setTransientProperty(String key,Object value) {
    if (transientProperties==null) {
      transientProperties = new HashMap<>();
    }
    this.transientProperties.put(key, value);
  }
  public void setTransientPropertyOpt(String key,Object value) {
    if (value==null) {
      return;
    }
    setTransientProperty(key, value);
  }
  public Object removeTransientProperty(String key) {
    return transientProperties!=null ? transientProperties.remove(key) : null;
  }
}
