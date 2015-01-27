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

import javax.script.ScriptEngineManager;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.TaskService;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.AsynchronousExecutorService;
import com.effektif.workflow.impl.ExpressionServiceImpl;
import com.effektif.workflow.impl.SimpleWorkflowCache;
import com.effektif.workflow.impl.SynchronousExecutorService;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.json.DefaultObjectMapperSupplier;
import com.effektif.workflow.impl.json.JacksonJsonService;
import com.effektif.workflow.impl.script.ScriptServiceImpl;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;


/** Configurations to build a workflow engine. */
public abstract class DefaultConfiguration implements Configuration {
  
  protected String id;
  protected Brewery brewery;
  
  public DefaultConfiguration() {
    brewery = new Brewery();
    brewery.ingredient(this);
    registerDefaultActivityTypeService();
    registerDefaultDataTypeService();
    registerDefaultExecutorService();
    registerDefaultExpressionService();
    registerDefaultJsonFactory();
    registerDefaultJsonService();
    registerDefaultObjectMapper();
    registerDefaultScriptManager();
    registerDefaultScriptService();
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
    brewery.ingredient(new ScriptServiceImpl());
  }

  protected void registerDefaultJsonService() {
    brewery.ingredient(new JacksonJsonService());
  }

  protected void registerDefaultActivityTypeService() {
    brewery.ingredient(new DefaultActivityTypeService());
  }
  
  protected void registerDefaultDataTypeService() {
    brewery.ingredient(new DefaultDataTypeService());
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

  public DefaultConfiguration id(String id) {
    this.id = id;
    return this;
  }
  
  public DefaultConfiguration synchronous() {
    brewery.ingredient(new SynchronousExecutorService());
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
