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
import com.effektif.workflow.api.activities.CallMapping;
import com.effektif.workflow.impl.activitytypes.CallImpl;
import com.effektif.workflow.impl.activitytypes.EmbeddedSubprocessImpl;
import com.effektif.workflow.impl.activitytypes.EndEventImpl;
import com.effektif.workflow.impl.activitytypes.ExclusiveGatewayImpl;
import com.effektif.workflow.impl.activitytypes.HttpServiceTaskImpl;
import com.effektif.workflow.impl.activitytypes.JavaServiceTaskImpl;
import com.effektif.workflow.impl.activitytypes.NoneTaskImpl;
import com.effektif.workflow.impl.activitytypes.ParallelGatewayImpl;
import com.effektif.workflow.impl.activitytypes.ScriptTaskImpl;
import com.effektif.workflow.impl.activitytypes.StartEventImpl;
import com.effektif.workflow.impl.activitytypes.UserTaskImpl;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.JacksonJsonService;
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
    initializeBeforeEngineCreation();
  }

  protected void initializeBeforeEngineCreation() {
    registerService(new ScriptEngineManager());
    registerService(new JsonFactory());
    registerService(new ObjectMapper());

    registerService(new Descriptors(serviceRegistry));
    registerService(new JacksonJsonService(serviceRegistry));
    registerService(new ScriptServiceImpl(serviceRegistry));
    registerService(new ExpressionServiceImpl(serviceRegistry));
    registerService(new ExecutorServiceImpl(serviceRegistry));
    registerService(new SimpleProcessDefinitionCache(serviceRegistry));

    registerDataType(new TextType());
    registerDataType(new NumberType());
    registerDataType(new ListType());
    registerJavaBeanType(CallMapping.class);

    registerActivityType(new StartEventImpl());
    registerActivityType(new EndEventImpl());
    registerActivityType(new EmbeddedSubprocessImpl());
    registerActivityType(new ExclusiveGatewayImpl());
    registerActivityType(new ParallelGatewayImpl());
    registerActivityType(new CallImpl());
    registerActivityType(new ScriptTaskImpl());
    registerActivityType(new UserTaskImpl());
    registerActivityType(new NoneTaskImpl());
    registerActivityType(new JavaServiceTaskImpl());
    registerActivityType(new HttpServiceTaskImpl());
  }

  protected void initializeAfterEngineCreation() {
  }

  public WorkflowEngine buildWorkflowEngine() {
    return new WorkflowEngineImpl(this);
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
