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
package com.effektif.workflow.api.types;

import java.lang.reflect.Type;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.TypeName;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Represents a JSON object type that internally is parsed to a Java bean.
 * 
 * @author Tom Baeyens
 */
@TypeName("javaBean")
public class JavaBeanType extends DataType {

  protected Class<?> javaClass;
  
  public JavaBeanType() {
  }
  
  public JavaBeanType(Class javaClass) {
    javaClass(javaClass);
  }
  
  public Class<?> getJavaClass() {
    return this.javaClass;
  }
  public void setJavaClass(Class<?> javaClass) {
    this.javaClass = javaClass;
  }
  public JavaBeanType javaClass(Class<?> javaClass) {
    this.javaClass = javaClass;
    return this;
  }

  @Override
  public Type getValueType() {
    return javaClass;
  }

  @Override
  public void readBpmn(BpmnReader r) {
    String className = r.readStringAttributeEffektif("class");
    try {
      javaClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Cannot read JavaBeanType BPMN - class not found: " + className);
    }
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    w.writeStringAttributeEffektif("class", javaClass.getName());
  }

  @Override
  public Object readBpmnValue(BpmnReader r) {
    throw new NotImplementedException();
  }

  @Override
  public void writeBpmnValue(BpmnWriter w, Object value) {
    throw new NotImplementedException();
  }
}
