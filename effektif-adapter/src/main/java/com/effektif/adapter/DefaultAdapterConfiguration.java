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
package com.effektif.adapter;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.data.DataTypeService;
import com.effektif.workflow.impl.json.JavaBeanValueMapper;
import com.effektif.workflow.impl.json.JsonStreamMapper;
import com.fasterxml.jackson.core.JsonFactory;


public class DefaultAdapterConfiguration implements Configuration {
  
  protected Brewery brewery;
  
  public DefaultAdapterConfiguration() {
    brewery = new Brewery();
    brewery.ingredient(this);
    brewery.ingredient(new JsonFactory());
    brewery.ingredient(new DataTypeService());
    brewery.ingredient(new JsonStreamMapper());
    brewery.ingredient(new JavaBeanValueMapper());
  }

  @Override
  public <T> T get(Class<T> type) {
    return brewery.get(type);
  }

  public Brewery getBrewery() {
    return this.brewery;
  }
  public void setBrewery(Brewery brewery) {
    this.brewery = brewery;
  }

  @Override
  public WorkflowEngine getWorkflowEngine() {
    return brewery.get(WorkflowEngine.class);
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
}
