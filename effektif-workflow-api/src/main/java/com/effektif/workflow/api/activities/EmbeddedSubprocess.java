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
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;

/**
 * Invokes a workflow that is embedded in another workflow.
 *
 * @see <a
 *      href="https://github.com/effektif/effektif/wiki/Embedded-Subprocess">Embedded
 *      Subprocess</a>
 * @author Tom Baeyens
 */
@TypeName("subProcessEmbedded")
@BpmnElement("subProcess")
public class EmbeddedSubprocess extends Activity {

  @Override
  public EmbeddedSubprocess id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public EmbeddedSubprocess multiInstance(MultiInstance multiInstance) {
    super.multiInstance(multiInstance);
    return this;
  }

  @Override
  public EmbeddedSubprocess transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public EmbeddedSubprocess transitionWithConditionTo(Condition condition, String toActivityId) {
    super.transitionWithConditionTo(condition, toActivityId);
    return this;
  }

  @Override
  public EmbeddedSubprocess transitionToNext() {
    super.transitionToNext();
    return this;
  }

  @Override
  public EmbeddedSubprocess transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public EmbeddedSubprocess activity(Activity activity) {
    super.activity(activity);
    return this;
  }

  @Override
  public EmbeddedSubprocess activity(String id, Activity activity) {
    super.activity(id, activity);
    return this;
  }

  @Override
  public EmbeddedSubprocess transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public EmbeddedSubprocess transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }

  @Override
  public EmbeddedSubprocess variable(Variable variable) {
    super.variable(variable);
    return this;
  }

  @Override
  public EmbeddedSubprocess timer(Timer timer) {
    super.timer(timer);
    return this;
  }

  @Override
  public EmbeddedSubprocess property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public EmbeddedSubprocess variable(String id, DataType type) {
    super.variable(id, type);
    return this;
  }

  @Override
  public EmbeddedSubprocess name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public EmbeddedSubprocess description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public EmbeddedSubprocess propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

  @Override
  public void readBpmn(BpmnReader r) {
    r.startScope(this);
    super.readBpmn(r);
    r.endScope();
  }
}
