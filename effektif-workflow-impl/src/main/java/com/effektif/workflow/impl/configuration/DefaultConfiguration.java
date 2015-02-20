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
package com.effektif.workflow.impl.configuration;

import javax.script.ScriptEngineManager;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.task.TaskService;
import com.effektif.workflow.impl.AsynchronousExecutorService;
import com.effektif.workflow.impl.SimpleWorkflowCache;
import com.effektif.workflow.impl.SynchronousExecutorService;
import com.effektif.workflow.impl.TaskServiceImpl;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.job.JobServiceImpl;
import com.effektif.workflow.impl.json.DefaultObjectMapperSupplier;
import com.effektif.workflow.impl.json.JsonService;
import com.effektif.workflow.impl.script.ExpressionServiceImpl;
import com.effektif.workflow.impl.script.StandardScriptService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;


/** Configurations to build a workflow engine. */
public abstract class DefaultConfiguration implements Configuration {
  
  protected Brewery brewery;
  
  public DefaultConfiguration() {
    brewery = new Brewery();
    brewery.ingredient(this);
    registerDefaultActivityTypeService();
    registerDefaultDataTypeService();
    registerDefaultExecutorService();
    registerDefaultExpressionService();
    registerDefaultJobService();
    registerDefaultJsonFactory();
    registerDefaultJsonService();
    registerDefaultObjectMapper();
    registerDefaultScriptManager();
    registerDefaultScriptService();
    registerDefaultTaskService();
    registerDefaultWorkflowCache();
    registerDefaultWorkflowEngine();
  }
  
  public WorkflowEngine getWorkflowEngine() {
    return brewery.get(WorkflowEngine.class);
  }

  public TaskService getTaskService() {
    return brewery.get(TaskService.class);
  }

  protected void registerDefaultWorkflowEngine() {
    brewery.ingredient(new WorkflowEngineConfiguration());
    brewery.ingredient(new WorkflowEngineImpl());
  }
  
  protected void registerDefaultWorkflowCache() {
    brewery.ingredient(new SimpleWorkflowCache());
  }

  protected void registerDefaultExecutorService() {
    brewery.ingredient(new AsynchronousExecutorService());
  }

  protected void registerDefaultExpressionService() {
    brewery.ingredient(new ExpressionServiceImpl());
  }

  protected void registerDefaultScriptService() {
    brewery.ingredient(new StandardScriptService());
  }

  protected void registerDefaultJsonService() {
    brewery.ingredient(new JsonService());
  }
  
  protected void registerDefaultJobService() {
    brewery.ingredient(new JobServiceImpl());
  }

  protected void registerDefaultActivityTypeService() {
    brewery.ingredient(new ActivityTypeService());
  }
  
  protected void registerDefaultDataTypeService() {
    brewery.ingredient(new DataTypeService());
  }
  
  protected void registerDefaultObjectMapper() {
    brewery.supplier(new DefaultObjectMapperSupplier(), ObjectMapper.class);
  }

  protected void registerDefaultJsonFactory() {
    brewery.ingredient(new JsonFactory());
  }

  protected void registerDefaultScriptManager() {
    brewery.ingredient(new ScriptEngineManager());
  }

  protected void registerDefaultTaskService() {
    brewery.ingredient(new TaskServiceImpl());
  }

  public DefaultConfiguration synchronous() {
    brewery.ingredient(new SynchronousExecutorService());
    return this;
  }

  public Brewery getBrewery() {
    return brewery;
  }

  @Override
  public <T> T get(Class<T> type) {
    return brewery.get(type);
  }
}
