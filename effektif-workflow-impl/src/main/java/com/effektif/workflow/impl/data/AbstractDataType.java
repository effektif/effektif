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
package com.effektif.workflow.impl.data;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.template.Hints;


public abstract class AbstractDataType<T extends DataType> implements DataTypeImpl<T> {
  
  protected T type;
  protected Class<? extends DataType> apiClass;
  protected Configuration configuration;

  public AbstractDataType(T type) {
    this.type = type;
    this.apiClass = type!=null ? type.getClass() : null;
  }
  
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
  
  @Override
  public boolean isStatic() {
    return true;
  }
  
  public Class<? extends DataType> getApiClass() {
    return apiClass;
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
  }

  @Override
  public DataTypeImpl parseDereference(String field, WorkflowParser parser) {
    return null;
  }

  @Override
  public TypedValueImpl dereference(Object value, String field) {
    return null;
  }

  @Override
  public T serialize() {
    return type;
  }

  @Override
  public String convertInternalToText(Object value, Hints hints) {
    return value!=null ? value.toString() : null;
  }

  /**
   * Returns a <code>String</code> binding with the value read from the attribute with the given name.
   */
  protected String readStringValue(XmlElement xml, String attributeName) {
    if (xml == null) {
      throw new IllegalArgumentException("null argument to method");
    }
    return xml.attributes.get(attributeName);
  }
  
  public T getDataType() {
    return type;
  }
}
