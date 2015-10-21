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
package com.effektif.workflow.impl.data.types;

import java.lang.reflect.Field;

import com.effektif.workflow.impl.data.DataTypeImpl;


/**
 * @author Tom Baeyens
 */
public class JavaBeanFieldImpl extends ObjectFieldImpl {

  protected Field field;

  public JavaBeanFieldImpl(Field field, DataTypeImpl dataType) {
    super(field.getName(), dataType);
    this.field = field;
    this.field.setAccessible(true);
  }

  public JavaBeanFieldImpl(String key) {
    super(key);
  }

  @Override
  public Object getFieldValue(Object value) {
    if (value==null) {
      return null;
    }
    try {
      return field.get(value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException("Couldn't dereference "+key+": "+e.getMessage(), e);
    }
  }
}
