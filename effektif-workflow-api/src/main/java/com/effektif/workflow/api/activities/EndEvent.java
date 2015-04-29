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
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Activity;


/**
 * Completes the incoming flow, and ends the whole workflow if there are no other active flows.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/End-Event">End Event</a>
 * @author Tom Baeyens
 */
@TypeName("endEvent")
@BpmnElement("endEvent")
public class EndEvent extends Activity {

  @Override
  public EndEvent id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public EndEvent property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public EndEvent name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public EndEvent description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public EndEvent propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }
}
