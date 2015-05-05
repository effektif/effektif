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
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Transition;


/**
 * A service task that consists of an HTTP interface.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/HTTP-Service-Task">HTTP Service Task</a>
 * @author Tom Baeyens
 */
@TypeName("httpServiceTask")
@BpmnElement("serviceTask")
@BpmnTypeAttribute(attribute="type", value="http")
public class HttpServiceTask extends ServiceTask {

  @Override
  public HttpServiceTask id(String id) {
    super.id(id);
    return this;
  }
  
  @Override
  public HttpServiceTask name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public HttpServiceTask description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public HttpServiceTask transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public HttpServiceTask transitionWithConditionTo(Condition condition, String toActivityId) {
    super.transitionWithConditionTo(condition, toActivityId);
    return this;
  }

  @Override
  public HttpServiceTask transitionToNext() {
    super.transitionToNext();
    return this;
  }

  @Override
  public HttpServiceTask transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public HttpServiceTask transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public HttpServiceTask transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }

  @Override
  public HttpServiceTask property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public HttpServiceTask propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }

}
