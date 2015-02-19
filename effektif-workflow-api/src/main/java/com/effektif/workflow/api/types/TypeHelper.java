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

import java.util.Collection;
import java.util.Iterator;


/**
 * @author Tom Baeyens
 */
public class TypeHelper {

  public static Type getTypeByValue(Object value) {
    if (value==null) {
      return null;
    }
    Class<?> valueClass = value.getClass();
    if (String.class.isAssignableFrom(valueClass)) {
      return new TextType();
    }
    if (Number.class.isAssignableFrom(valueClass)) {
      return new NumberType();
    }
    if (Collection.class.isAssignableFrom(valueClass)) {
      ListType listType = new ListType()
        .elementType(getTypeByElements((Collection)value));
      return listType;

    } else {
      return new JavaBeanType(valueClass);
    }
  }

  protected static Type getTypeByElements(Collection collection) {
    if (collection.isEmpty()) {
      return null;
    }
    Iterator iterator = collection.iterator();
    Object firstValue = iterator.next();
    return getTypeByValue(firstValue);
  }
}
