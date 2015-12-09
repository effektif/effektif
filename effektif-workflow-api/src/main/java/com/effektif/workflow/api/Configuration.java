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
package com.effektif.workflow.api;


/**
 * A workflow engine runtime configuration.
 * Instantiate one of the implementing classes like
 * <code>com.effektif.workflow.impl.memory.MemoryConfiguration</code> or a
 * <code>com.effektif.mongo.MongoConfiguration</code>
 * to get started configuring your workflow engine.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Runtime-configuration">Runtime configuration</a>
 * @author Tom Baeyens
 */
public interface Configuration {
  
  WorkflowEngine getWorkflowEngine();
  
  <T> T get(Class<T> type);
  Object get(String name);
  void set(Object bean, String name);
  void set(Object bean);

  void start();
  void stop();
}
