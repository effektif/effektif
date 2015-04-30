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

import com.effektif.workflow.api.json.TypeName;


/** 
 * represents a json object type that internally is parsed to a java bean. 
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
  
//  @Override
//  public void readJson(JsonReader r) {
//    javaClass = r.readClass("javaClass");
//    super.readJson(r);
//  }
//
//  @Override
//  public void writeJson(JsonWriter w) {
//    super.writeJson(w);
//    w.writeClass("javaClass", javaClass);
//  }

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

}
