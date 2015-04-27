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
package com.effektif.workflow.impl.slack;

import com.effektif.workflow.api.serialization.json.TypeName;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Element;
import com.effektif.workflow.api.workflow.MultiInstance;
import com.effektif.workflow.api.workflow.Scope;
import com.effektif.workflow.api.workflow.Timer;
import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.api.workflow.Variable;


/**
 * @author Tom Baeyens
 */
@TypeName("slackPost")
public class SlackPost extends Activity {

  protected String slackAccountId;
  protected String channel;
  protected String message;
  // TODO later move to protected Binding<String> message;  or some other templating mechanism like String.format or external library

  public String getChannel() {
    return this.channel;
  }
  public void setChannel(String channel) {
    this.channel = channel;
  }
  public SlackPost channel(String channel) {
    this.channel = channel;
    return this;
  }

  public String getMessage() {
    return this.message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public SlackPost message(String message) {
    this.message = message;
    return this;
  }

  public String getSlackAccountId() {
    return this.slackAccountId;
  }
  public void setSlackAccountId(String slackAccountId) {
    this.slackAccountId = slackAccountId;
  }
  public SlackPost slackAccountId(String slackAccountId) {
    this.slackAccountId = slackAccountId;
    return this;
  }
  
  
  @Override
  public SlackPost name(String name) {
    super.name(name);
    return this;
  }
  
  
  @Override
  public Activity multiInstance(MultiInstance multiInstance) {
    return super.multiInstance(multiInstance);
  }
  @Override
  public Activity transitionTo(String toActivityId) {
    return super.transitionTo(toActivityId);
  }
  @Override
  public Activity transitionToNext() {
    return super.transitionToNext();
  }
  @Override
  public Activity transitionTo(Transition transition) {
    return super.transitionTo(transition);
  }
  @Override
  public Activity activity(Activity activity) {
    return super.activity(activity);
  }
  @Override
  public Activity activity(String id, Activity activity) {
    return super.activity(id, activity);
  }
  @Override
  public Activity transition(Transition transition) {
    return super.transition(transition);
  }
  @Override
  public Activity transition(String id, Transition transition) {
    return super.transition(id, transition);
  }
  @Override
  public Activity variable(Variable variable) {
    return super.variable(variable);
  }
  @Override
  public Activity timer(Timer timer) {
    return super.timer(timer);
  }
  @Override
  public Activity property(String key, Object value) {
    return super.property(key, value);
  }
  @Override
  public Scope variable(String id, Type type) {
    return super.variable(id, type);
  }
  @Override
  public Element description(String description) {
    return super.description(description);
  }
}
