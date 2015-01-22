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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.script.ScriptEngineManager;

import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.activities.CallMapping;
import com.effektif.workflow.impl.activities.CallImpl;
import com.effektif.workflow.impl.activities.EmailTaskImpl;
import com.effektif.workflow.impl.activities.EmbeddedSubprocessImpl;
import com.effektif.workflow.impl.activities.EndEventImpl;
import com.effektif.workflow.impl.activities.ExclusiveGatewayImpl;
import com.effektif.workflow.impl.activities.HttpServiceTaskImpl;
import com.effektif.workflow.impl.activities.JavaServiceTaskImpl;
import com.effektif.workflow.impl.activities.NoneTaskImpl;
import com.effektif.workflow.impl.activities.ParallelGatewayImpl;
import com.effektif.workflow.impl.activities.ScriptTaskImpl;
import com.effektif.workflow.impl.activities.StartEventImpl;
import com.effektif.workflow.impl.activities.UserTaskImpl;
import com.effektif.workflow.impl.adapter.AdapterConnection;
import com.effektif.workflow.impl.adapter.AdapterService;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.JacksonJsonService;
import com.effektif.workflow.impl.plugin.ActivityType;
import com.effektif.workflow.impl.plugin.ActivityTypeService;
import com.effektif.workflow.impl.plugin.Initializable;
import com.effektif.workflow.impl.plugin.ServiceRegistry;
import com.effektif.workflow.impl.script.ScriptServiceImpl;
import com.effektif.workflow.impl.task.TaskService;
import com.effektif.workflow.impl.type.DataType;
import com.effektif.workflow.impl.type.DataTypeService;
import com.effektif.workflow.impl.types.BindingTypeImpl;
import com.effektif.workflow.impl.types.JavaBeanTypeImpl;
import com.effektif.workflow.impl.types.ListTypeImpl;
import com.effektif.workflow.impl.types.NumberTypeImpl;
import com.effektif.workflow.impl.types.TextTypeImpl;
import com.effektif.workflow.impl.types.UserReferenceTypeImpl;
import com.effektif.workflow.impl.types.VariableReferenceTypeImpl;
import com.effektif.workflow.impl.types.WorkflowReferenceTypeImpl;
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
 * WorkflowEngineConfiguration configuration = new MemoryWorkflowEngineConfiguration()
 *   .registerDataType(new MyCustomDataType())
 *   .registerJavaBeanType(MyCustomJavaBean.class)
 *   .registerActivityType(new MyCustomActivityType())
 *   .registerService(new MyCustomServiceObject())
 *   .initialize();
 *   
 * WorkflowEngine workflowEngine = configuration.getWorkflowEngine();
 * TaskService taskService = configuration.getTaskService();
 * }</pre>
 */
public abstract class WorkflowEngineConfiguration {
  
  protected boolean isInitialized;
  protected String id;
  protected ServiceRegistry serviceRegistry;
  protected List<Initializable> initializables = new ArrayList<>();
  protected List<ActivityType> activityTypes = new ArrayList<>();
  protected List<DataType> types = new ArrayList<>();
  protected List<Class<? extends JobType>> jobTypeClasses = new ArrayList<>();
  protected List<Class<?>> javaBeanTypes = new ArrayList<>();
  protected List<AdapterConnection> adapterConnections = new ArrayList<>();

  public WorkflowEngineConfiguration() {
    this(new SimpleServiceRegistry());
  }

  protected void configureDefaults() {
    configureDefaultWorkflowEngine();
    configureDefaultId();
    configureDefaultObjectMapper();
    configureDefaultJsonFactory();
    configureDefaultActivityTypeService();
    configureDefaultDataTypeService();
    configureDefaultAdapterService();
    configureDefaultActivityTypes();
    configureDefaultDataTypes();
    configureDefaultScriptManager();
    configureDefaultJsonService();
    configureDefaultScriptService();
    configureDefaultExpressionService();
    configureDefaultExecutorService();
    configureDefaultWorkflowCache();
  }
  
  protected void configureDefaultActivityTypes() {
    registerActivityType(new StartEventImpl());
    registerActivityType(new EndEventImpl());
    registerActivityType(new EmailTaskImpl());
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

  protected void configureDefaultDataTypes() {
    registerDataType(new BindingTypeImpl());
    registerDataType(new JavaBeanTypeImpl());
    registerDataType(new NumberTypeImpl());
    registerDataType(new ListTypeImpl());
    registerDataType(new TextTypeImpl());
    registerDataType(new UserReferenceTypeImpl());
    registerDataType(new VariableReferenceTypeImpl());
    registerDataType(new WorkflowReferenceTypeImpl());
    registerJavaBeanType(CallMapping.class);
  }
  
  protected WorkflowEngineConfiguration(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
  
  /** must be invoked before the workflow engine and task service are retrieved,
   * and no further configurations can be set after this method is called.
   * @return this so it can be used in a fluent style. */
  public WorkflowEngineConfiguration initialize() {
    if (!isInitialized) {
      isInitialized = true;
      for (Initializable initializable : initializables) {
        initializable.initialize(serviceRegistry, this);
      }
      DataTypeService dataTypeService = serviceRegistry.getService(DataTypeService.class);
      for (Class< ? > javaBeanType : javaBeanTypes) {
        dataTypeService.registerJavaBeanType(javaBeanType);
      }
      for (DataType type : types) {
        dataTypeService.registerDataType(type);
      }
      ActivityTypeService activityTypeService = serviceRegistry.getService(ActivityTypeService.class);
      for (ActivityType activityType : activityTypes) {
        activityTypeService.registerActivityType(activityType);
      }
      for (Class< ? extends JobType> jobTypeClass : jobTypeClasses) {
        activityTypeService.registerJobType(jobTypeClass);
      }
      AdapterService adapterService = serviceRegistry.getService(AdapterService.class);
      for (AdapterConnection adapterConnection : adapterConnections) {
        adapterService.registerAdapterConnection(adapterConnection);
      }
    }
    return this;
  }
  
  public WorkflowEngine getWorkflowEngine() {
    checkInitialized();
    return serviceRegistry.getService(WorkflowEngine.class);
  }

  public TaskService getTaskService() {
    checkInitialized();
    return serviceRegistry.getService(TaskService.class);
  }
  
  protected void configureDefaultWorkflowEngine() {
    registerService(new WorkflowEngineImpl());
  }

  protected void configureDefaultId() {
    if (id==null) {
      try {
        id = InetAddress.getLocalHost().getHostAddress();
        try {
          String processName = ManagementFactory.getRuntimeMXBean().getName();
          int atIndex = processName.indexOf('@');
          if (atIndex > 0) {
            id += ":" + processName.substring(0, atIndex);
          }
        } catch (Exception e) {
          id += ":?";
        }
      } catch (UnknownHostException e1) {
        id = UUID.randomUUID().toString();
      }
    }
  }

  protected void configureDefaultWorkflowCache() {
    registerService(new SimpleWorkflowCache());
  }

  protected void configureDefaultExecutorService() {
    registerService(new AsynchronousExecutorService());
  }

  protected void configureDefaultExpressionService() {
    registerService(new ExpressionServiceImpl());
  }

  protected void configureDefaultScriptService() {
    registerService(new ScriptServiceImpl());
  }

  protected void configureDefaultJsonService() {
    registerService(new JacksonJsonService());
  }

  protected void configureDefaultActivityTypeService() {
    registerService(new ActivityTypeService());
  }
  
  protected void configureDefaultDataTypeService() {
    registerService(new DataTypeService());
  }
  
  protected void configureDefaultAdapterService() {
    registerService(new AdapterService());
  }
  
  protected void configureDefaultObjectMapper() {
    registerService(new ObjectMapper());
  }

  protected void configureDefaultJsonFactory() {
    registerService(new JsonFactory());
  }

  protected void configureDefaultScriptManager() {
    registerService(new ScriptEngineManager());
  }

  public WorkflowEngineConfiguration registerService(Object service) {
    checkUninitialized();
    serviceRegistry.registerService(service);
    if (service instanceof Initializable) {
      initializables.add((Initializable)service);
    }
    return this;
  }
  
  public WorkflowEngineConfiguration synchronous() {
    checkUninitialized();
    registerService(new SynchronousExecutorService());
    return this;
  }

  public WorkflowEngineConfiguration registerJavaBeanType(Class<?> javaBeanType) {
    checkUninitialized();
    javaBeanTypes.add(javaBeanType);
    return this;
  }

  public WorkflowEngineConfiguration registerActivityType(ActivityType activityType) {
    checkUninitialized();
    activityTypes.add(activityType);
    return this;
  }

  public WorkflowEngineConfiguration registerDataType(DataType type) {
    checkUninitialized();
    types.add(type);
    return this;
  }

  public WorkflowEngineConfiguration registerJobType(Class<? extends JobType> jobTypeClass) {
    checkUninitialized();
    jobTypeClasses.add(jobTypeClass);
    return this;
  }

  public WorkflowEngineConfiguration registerAdapter(String adapterUrl) {
    registerAdapter(new AdapterConnection().url(adapterUrl));
    return this;
  }

  public WorkflowEngineConfiguration registerAdapter(AdapterConnection adapterConnection) {
    checkUninitialized();
    adapterConnections.add(adapterConnection);
    return this;
  }


  public WorkflowEngineConfiguration id(String id) {
    checkUninitialized();
    this.id = id;
    return this;
  }

  public void setId(String id) {
    checkUninitialized();
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  protected void checkInitialized() {
    if (!isInitialized) {
      throw new RuntimeException("Please, initialize the configuration first with workflowEngineConfiguration.initialize()");
    }
  }
  
  protected void checkUninitialized() {
    if (isInitialized) {
      throw new RuntimeException("This configuration is already initialized.  Please perform all configurations before invoking .initialized()");
    }
  }
}
