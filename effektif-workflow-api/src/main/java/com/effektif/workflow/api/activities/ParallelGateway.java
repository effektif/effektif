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

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Transition;


/**
 * A parallel gateway, used to fork and join sequence flows for executing tasks in parallel.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Parallel-Gateway">Parallel Gateway</a>
 * @author Tom Baeyens
 */
@TypeName("parallelGateway")
@BpmnElement("parallelGateway")
public class ParallelGateway extends Activity {

  @Override
  public ParallelGateway id(String id) {
    super.id(id);
    return this;
  }
  
  @Override
  public ParallelGateway name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public ParallelGateway description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public ParallelGateway transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public ParallelGateway transitionWithConditionTo(Condition condition, String toActivityId) {
    super.transitionWithConditionTo(condition, toActivityId);
    return this;
  }

  @Override
  public ParallelGateway transitionToNext() {
    super.transitionToNext();
    return this;
  }

  @Override
  public ParallelGateway transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public ParallelGateway transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public ParallelGateway transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }

  @Override
  public ParallelGateway property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public ParallelGateway propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

}
