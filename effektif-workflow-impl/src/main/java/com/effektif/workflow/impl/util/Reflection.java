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
package com.effektif.workflow.impl.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Tom Baeyens
 */
public class Reflection {
  
  public static List<Field> getNonStaticFieldsRecursive(Class< ? > type) {
    List<Field> fieldCollector = new ArrayList<>();
    collectNonStaticFieldsRecursive(type, fieldCollector);
    return fieldCollector;
  }

  static void collectNonStaticFieldsRecursive(Class< ? > type, List<Field> fieldCollector) {
    Field[] fields = type.getDeclaredFields();
    if (fields!=null) {
      for (Field field: fields) {
        if (!Modifier.isStatic(field.getModifiers())) {
          fieldCollector.add(field);
        }
      }
    }
    Class< ? > superclass = type.getSuperclass();
    if (superclass!=null && superclass!=Object.class) {
      collectNonStaticFieldsRecursive(superclass, fieldCollector);
    }
  }

  public static <T> T newInstance(Class<T> type) {
    if (type==null) {
      return null;
    }
    try {
      return type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+type+" with the default constructor: "+e.getMessage(), e);
    }
  }
}
