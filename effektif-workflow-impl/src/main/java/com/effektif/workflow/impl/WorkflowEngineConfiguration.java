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
package com.effektif.workflow.impl;

import javax.script.ScriptEngineManager;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.activities.CallMapping;
import com.effektif.workflow.api.activities.DefaultTask;
import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.impl.ExecutorServiceImpl;
import com.effektif.workflow.impl.SimpleProcessDefinitionCache;
import com.effektif.workflow.impl.SimpleServiceRegistry;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.JacksonJsonService;
import com.effektif.workflow.impl.memory.MemoryWorkflowEngine;
import com.effektif.workflow.impl.plugin.ActivityType;
import com.effektif.workflow.impl.plugin.Descriptors;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.script.ScriptServiceImpl;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.type.ListType;
import com.effektif.workflow.impl.type.NumberType;
import com.effektif.workflow.impl.type.TextType;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;


/** Configurations to build a workflow engine.
 * 
 * By default, this configuration implementation produces a {@link MemoryWorkflowEngine}.
 * 
 * Subclasses of this configuration defined in other modules can produce other workflow engines.
 * E.g. MongoWorkflowEngineConfiguration or ClientWorkflowEngineConfiguration. 
 * 
 * <pre>{@code
 * WorkflowEngine workflowEngine = new WorkflowEngineConfiguration()
 *   .registerDataType(new MyCustomDataType())
 *   .registerJavaBeanType(MyCustomJavaBean.class)
 *   .registerActivityType(new MyCustomActivityType())
 *   .registerService(new MyCustomServiceObject())
 *   .buildWorkflowEngine();
 * }</pre>
 * 
 * 
 * @author Walter White
 */
public class WorkflowEngineConfiguration {
  
  protected String id;
  protected ServiceRegistry serviceRegistry;
  
  public WorkflowEngineConfiguration() {
    this(new SimpleServiceRegistry());
  }
  
  public WorkflowEngineConfiguration(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
    initializeDefaultStorageServices();
    initializeDefaultInternalServices();
    initializeDefaultDataTypes();
    initializeDefaultActivityTypes();
  }

  protected void initializeDefaultStorageServices() {
  }

  protected void initializeDefaultInternalServices() {
    initializeObjectMapper();
    initializeJsonFactory();
    initializeScriptEngineManager();
    registerService(new Descriptors(serviceRegistry));
    registerService(new JacksonJsonService(serviceRegistry));
    registerService(new ScriptServiceImpl(serviceRegistry));
    registerService(new ExecutorServiceImpl(serviceRegistry));
    registerService(new SimpleProcessDefinitionCache(serviceRegistry));
  }

  protected void initializeScriptEngineManager() {
    registerService(new ScriptEngineManager());
  }

  protected void initializeJsonFactory() {
    registerService(new JsonFactory());
  }

  protected void initializeObjectMapper() {
    registerService(new ObjectMapper());
  }
    
  protected void initializeDefaultDataTypes() {
    this.registerDataType(new TextType());
    this.registerDataType(new NumberType());
    this.registerDataType(new ListType());
    this.registerJavaBeanType(CallMapping.class);
  }
  
  protected void initializeDefaultActivityTypes() {
    this.registerActivityType(new StartEvent());
    this.registerActivityType(new EndEvent());
    this.registerActivityType(new EmbeddedSubprocess());
    this.registerActivityType(new ExclusiveGateway());
    this.registerActivityType(new ParallelGateway());
    this.registerActivityType(new Call());
    this.registerActivityType(new ScriptTask());
    this.registerActivityType(new UserTask());
    this.registerActivityType(new DefaultTask());
    this.registerActivityType(new JavaServiceTask());
    this.registerActivityType(new HttpServiceTask());
  }

  public WorkflowEngine buildWorkflowEngine() {
    return new MemoryWorkflowEngine(this);
  }

  public WorkflowEngineConfiguration registerService(Object service) {
    serviceRegistry.registerService(service);
    return this;
  }
  
  public WorkflowEngineConfiguration registerJavaBeanType(Class<?> javaBeanType) {
    getDescriptors().registerJavaBeanType(javaBeanType);
    return this;
  }

  public WorkflowEngineConfiguration registerActivityType(ActivityType activityType) {
    getDescriptors().registerActivityType(activityType);
    return this;
  }

  public WorkflowEngineConfiguration registerDataType(DataType dataType) {
    getDescriptors().registerDataType(dataType);
    return this;
  }

  public WorkflowEngineConfiguration registerJobType(Class<? extends JobType> jobTypeClass) {
    getDescriptors().registerJobType(jobTypeClass);
    return this;
  }

  protected Descriptors getDescriptors() {
    return serviceRegistry.getService(Descriptors.class);
  }
  
  public WorkflowEngineConfiguration id(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}
