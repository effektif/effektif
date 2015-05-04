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
import com.effektif.workflow.api.deprecated.task.TaskService;
import com.effektif.workflow.impl.AsynchronousExecutorService;
import com.effektif.workflow.impl.ConditionServiceImpl;
import com.effektif.workflow.impl.SimpleWorkflowCache;
import com.effektif.workflow.impl.SynchronousExecutorService;
import com.effektif.workflow.impl.WorkflowEngineConfiguration;
import com.effektif.workflow.impl.WorkflowEngineImpl;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.deprecated.CaseServiceImpl;
import com.effektif.workflow.impl.deprecated.TaskServiceImpl;
import com.effektif.workflow.impl.deprecated.email.OutgoingEmailServiceImpl;
import com.effektif.workflow.impl.deprecated.email.OutgoingEmailServiceSupplier;
import com.effektif.workflow.impl.deprecated.json.JsonMapper;
import com.effektif.workflow.impl.deprecated.json.Mappings;
import com.effektif.workflow.impl.deprecated.script.RhinoScriptService;
import com.effektif.workflow.impl.job.JobServiceImpl;
import com.effektif.workflow.impl.json.JavaBeanValueMapper;
import com.effektif.workflow.impl.json.JsonStreamMapper;


/** Configurations to build a workflow engine. */
public abstract class DefaultConfiguration implements Configuration {
  
  protected Brewery brewery;
  
  public DefaultConfiguration() {
    brewery = new Brewery();
    brewery.ingredient(this);

    brewery.ingredient(new WorkflowEngineConfiguration());
    brewery.ingredient(new WorkflowEngineImpl());
    brewery.ingredient(new SimpleWorkflowCache());
    brewery.ingredient(new AsynchronousExecutorService());
    brewery.ingredient(new ConditionServiceImpl());
    brewery.ingredient(new JobServiceImpl());
    brewery.ingredient(new ActivityTypeService());
    brewery.ingredient(new DataTypeService());
    brewery.ingredient(new JsonStreamMapper());
    brewery.ingredient(new JavaBeanValueMapper());

    // deprecated
    brewery.ingredient(new CaseServiceImpl());
    brewery.ingredient(new RhinoScriptService());
    brewery.ingredient(new JsonMapper());
    brewery.ingredient(new Mappings());
    brewery.ingredient(new ScriptEngineManager());
    brewery.ingredient(new TaskServiceImpl());
    brewery.supplier(new OutgoingEmailServiceSupplier(), OutgoingEmailServiceImpl.class);
  }

  public WorkflowEngine getWorkflowEngine() {
    return brewery.get(WorkflowEngine.class);
  }

  public TaskService getTaskService() {
    return brewery.get(TaskService.class);
  }

  
  public DefaultConfiguration ingredient(Object ingredient) {
    brewery.ingredient(ingredient);
    return this;
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
