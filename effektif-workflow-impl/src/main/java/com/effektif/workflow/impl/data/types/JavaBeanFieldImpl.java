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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.types.ObjectField;
import com.effektif.workflow.impl.data.TypedValueImpl;


/**
 * @author Tom Baeyens
 */
public class JavaBeanFieldImpl extends ObjectFieldImpl {

  protected Field field;

  public JavaBeanFieldImpl(Class< ? > objectClass, ObjectField fieldApi, Configuration configuration) {
    super(objectClass, fieldApi, configuration);
    try {
      this.field = objectClass.getDeclaredField(name);
      this.field.setAccessible(true);
    } catch (IllegalArgumentException | NoSuchFieldException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  public JavaBeanFieldImpl(String name) {
    super(name);
  }

  @Override
  public void dereferenceValue(TypedValueImpl typedValue) {
    try {
      typedValue.value = field.get(typedValue.value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException("Couldn't dereference "+name+": "+e.getMessage(), e);
    }
  }

}
