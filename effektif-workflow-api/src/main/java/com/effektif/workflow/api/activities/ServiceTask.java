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
package com.effektif.workflow.api.activities;

/**
 * A service task is a call to code that will execute outside the service engine.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Service-Task">Service Task</a>
 * @author Tom Baeyens
 */
public abstract class ServiceTask extends NoneTask {

  @Override
  public ServiceTask id(String id) {
    super.id(id);
    return this;
  }

}
