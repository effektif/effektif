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
package com.effektif.workflow.impl;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.activity.ActivityTypeService;
import com.effektif.workflow.impl.bpmn.BpmnMapper;
import com.effektif.workflow.impl.bpmn.BpmnMapperSupplier;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.job.JobServiceImpl;
import com.effektif.workflow.impl.job.TimerTypeService;
import com.effektif.workflow.impl.json.JavaBeanValueMapper;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.effektif.workflow.impl.json.configuration.JavaBeanValueMapperSupplier;
import com.effektif.workflow.impl.json.configuration.JavaBeanValueMappingsBuilder;
import com.effektif.workflow.impl.json.configuration.JsonStreamMapperSupplier;
import com.effektif.workflow.impl.json.configuration.JsonStreamMappingsBuilder;


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
    brewery.ingredient(new TimerTypeService());
    brewery.ingredient(new JsonStreamMappingsBuilder());
    brewery.ingredient(new JavaBeanValueMappingsBuilder());

    brewery.supplier(new JsonStreamMapperSupplier(), JsonStreamMapper.class);
    brewery.supplier(new JavaBeanValueMapperSupplier(), JavaBeanValueMapper.class);
    brewery.supplier(new BpmnMapperSupplier(), BpmnMapper.class);
  }

  public WorkflowEngine getWorkflowEngine() {
    return brewery.get(WorkflowEngine.class);
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
  
  public void setBrewery(Brewery brewery) {
    this.brewery = brewery;
  }


  @Override
  public <T> T get(Class<T> type) {
    return brewery.get(type);
  }

  @Override
  public Object get(String name) {
    return brewery.get(name);
  }
  
  @Override
  public void set(Object bean, String name) {
    brewery.ingredient(bean, name);
  }

  @Override
  public void set(Object bean) {
    brewery.ingredient(bean);
  }

  @Override
  public void start() {
    brewery.start();
  }

  @Override
  public void stop() {
    brewery.stop();
  }
}
