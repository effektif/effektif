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
package com.effektif.workflow.impl.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineManager;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.TaskService;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.AsynchronousExecutorService;
import com.effektif.workflow.impl.ExpressionServiceImpl;
import com.effektif.workflow.impl.SimpleWorkflowCache;
import com.effektif.workflow.impl.SynchronousExecutorService;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.activity.ActivityType;
import com.effektif.workflow.impl.adapter.AdapterService;
import com.effektif.workflow.impl.data.DataType;
import com.effektif.workflow.impl.job.JobType;
import com.effektif.workflow.impl.json.JacksonJsonService;
import com.effektif.workflow.impl.script.ScriptServiceImpl;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;


/** Configurations to build a workflow engine. */
public abstract class DefaultConfiguration implements Configuration {
  
  protected String id;
  protected Brewery brewery;
  
  protected List<Initializable> initializables = new ArrayList<>();
  protected List<ActivityType> activityTypes = new ArrayList<>();
  protected List<DataType> types = new ArrayList<>();
  protected List<Class<? extends JobType>> jobTypeClasses = new ArrayList<>();
  protected List<Class<?>> javaBeanTypes = new ArrayList<>();

  public DefaultConfiguration() {
    brewery = new Brewery();
    brewery.register(this);
    registerDefaultWorkflowEngine();
    registerDefaultObjectMapper();
    registerDefaultJsonFactory();
    registerDefaultActivityTypeService();
    registerDefaultDataTypeService();
    registerDefaultAdapterService();
    registerDefaultScriptManager();
    registerDefaultExpressionService();
    registerDefaultJsonService();
    registerDefaultScriptService();
    registerDefaultExecutorService();
    registerDefaultWorkflowCache();
  }
  
  public WorkflowEngine getWorkflowEngine() {
    return brewery.get(WorkflowEngine.class);
  }

  public TaskService getTaskService() {
    return brewery.get(TaskService.class);
  }
  
  protected void registerDefaultWorkflowEngine() {
    brewery.register(new WorkflowEngineConfiguration());
    brewery.register(new WorkflowEngineImpl());
  }
  
  protected void registerDefaultWorkflowCache() {
    brewery.register(new SimpleWorkflowCache());
  }

  protected void registerDefaultExecutorService() {
    brewery.register(new AsynchronousExecutorService());
  }

  protected void registerDefaultExpressionService() {
    brewery.register(new ExpressionServiceImpl());
  }

  protected void registerDefaultScriptService() {
    brewery.register(new ScriptServiceImpl());
  }

  protected void registerDefaultJsonService() {
    brewery.register(new JacksonJsonService());
  }

  protected void registerDefaultActivityTypeService() {
    brewery.register(new DefaultActivityTypeService());
  }
  
  protected void registerDefaultDataTypeService() {
    brewery.register(new DefaultDataTypeService());
  }
  
  protected void registerDefaultAdapterService() {
    brewery.register(new AdapterService());
  }
  
  protected void registerDefaultObjectMapper() {
    brewery.register(new ObjectMapper());
  }

  protected void registerDefaultJsonFactory() {
    brewery.register(new JsonFactory());
  }

  protected void registerDefaultScriptManager() {
    brewery.register(new ScriptEngineManager());
  }

  public DefaultConfiguration id(String id) {
    this.id = id;
    return this;
  }
  
  public DefaultConfiguration synchronous() {
    brewery.register(new SynchronousExecutorService());
    return this;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public Brewery getRegistry() {
    return brewery;
  }

  @Override
  public <T> T get(Class<T> type) {
    return brewery.get(type);
  }
}
