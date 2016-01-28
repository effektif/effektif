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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.api.workflow.Transition;


/** 
 * invokes a java method.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Java-Service-Task">Java Service Task</a>
 * @author Tom Baeyens
 */
@TypeName("javaServiceTask")
@BpmnElement("serviceTask")
@BpmnTypeAttribute(attribute="type", value="java")
public class JavaServiceTask extends ServiceTask {
  
  /** the name of the bean in the brewery (engine configuration) to invoke the method on.
   * This is mutually exclusive with clazz. */
  protected String beanName;

  /** The class on which the static method will be invoked.
   * This is mutually exclusive with the beanName property. */
  protected Class javaClass;
  
  protected String methodName;
  
  protected List<Binding> argBindings;
  
  /** the name of the bean in the brewery (engine configuration) to invoke the method on.
   * This is mutually exclusive with clazz. */
  public String getBeanName() {
    return this.beanName;
  }
  /** the name of the bean in the brewery (engine configuration) to invoke the method on.
   * This is mutually exclusive with clazz. */
  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }
  /** the name of the bean in the brewery (engine configuration) to invoke the method on.
   * This is mutually exclusive with clazz. */
  public JavaServiceTask beanName(String beanName) {
    this.beanName = beanName;
    return this;
  }
  
  /** The class on which the static method will be invoked.
   * This is mutually exclusive with the beanName property. */
  public Class getJavaClass() {
    return this.javaClass;
  }
  /** The class on which the static method will be invoked.
   * This is mutually exclusive with the beanName property. */
  public void setJavaClass(Class javaClass) {
    this.javaClass = javaClass;
  }
  /** The class on which the static method will be invoked.
   * This is mutually exclusive with the beanName property. */
  public JavaServiceTask javaClass(Class javaClass) {
    this.javaClass = javaClass;
    return this;
  }
  
  public String getMethodName() {
    return this.methodName;
  }
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  public JavaServiceTask methodName(String methodName) {
    this.methodName = methodName;
    return this;
  }
  
  public List<Binding> getArgBindings() {
    return this.argBindings;
  }
  public void setArgBindings(List<Binding> argBindings) {
    this.argBindings = argBindings;
  }
  public JavaServiceTask addArgBinding(Binding argBinding) {
    if (this.argBindings==null) {
      this.argBindings = new ArrayList<>();
    }
    this.argBindings.add(argBinding);
    return this;
  }
  public JavaServiceTask argValue(Object value) {
    addArgBinding(new Binding().value(value));
    return this;
  }
  public JavaServiceTask argExpression(String expression) {
    addArgBinding(new Binding().expression(expression));
    return this;
  }
  public JavaServiceTask argTemplate(String template) {
    addArgBinding(new Binding().template(template));
    return this;
  }

  public JavaServiceTask id(String id) {
    super.id(id);
    return this;
  }
  
  @Override
  public JavaServiceTask name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public JavaServiceTask description(String description) {
    super.description(description);
    return this;
  }

  @Override
  public JavaServiceTask transitionTo(String toActivityId) {
    super.transitionTo(toActivityId);
    return this;
  }

  @Override
  public JavaServiceTask transitionWithConditionTo(Condition condition, String toActivityId) {
    super.transitionWithConditionTo(condition, toActivityId);
    return this;
  }

  @Override
  public JavaServiceTask transitionToNext() {
    super.transitionToNext();
    return this;
  }

  @Override
  public JavaServiceTask transitionTo(Transition transition) {
    super.transitionTo(transition);
    return this;
  }

  @Override
  public JavaServiceTask transition(Transition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public JavaServiceTask transition(String id, Transition transition) {
    super.transition(id, transition);
    return this;
  }

  @Override
  public JavaServiceTask property(String key, Object value) {
    super.property(key, value);
    return this;
  }

  @Override
  public JavaServiceTask propertyOpt(String key, Object value) {
    super.propertyOpt(key, value);
    return this;
  }
}
