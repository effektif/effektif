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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.json.GenericType;
import com.effektif.workflow.impl.deprecated.json.AbstractJsonReader;


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

  public static Class< ? > loadClass(String className) {
    Class<?> clazz = null;
    if (className!=null) {
      try {
        clazz = Class.forName(className);
      } catch (ClassNotFoundException e) {
        AbstractJsonReader.log.debug("Class not found with effektif classloader: "+className+". Trying context classloader...");
        try {
          clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e1) {
          AbstractJsonReader.log.debug("Class not found with context classloader: "+className+". Giving up.");
        }
      }
    }
    return clazz;
  }

  public static Class< ? > getClass(Type type) {
    Class<?> clazz = null;
    if (type instanceof Class) {
      clazz = (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      clazz = (Class< ? >) parameterizedType.getRawType();
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      clazz = (Class< ? >) wildcardType.getUpperBounds()[0];
    } else if (type instanceof GenericType) {
      clazz = ((GenericType)type).getRawClass();
    }
    return clazz;
  }

  public static Type getTypeArg(Type type, int i) {
    if (type instanceof ParameterizedType) {
      Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();
      if (typeArgs!=null && i<typeArgs.length) {
        return typeArgs[i];
      }
    } else if (type instanceof GenericType) {
      Type[] typeArgs = ((GenericType) type).getTypeArgs();
      if (typeArgs!=null && i<typeArgs.length) {
        return typeArgs[i];
      }
    }
    return null;
  }

  public static Type resolveFieldType(TypeVariable fieldType, Class<?> clazz, Type type) {
    Map<String,Type> typeArgs = new HashMap<>();
    TypeVariable< ? >[] typeParameters = clazz.getTypeParameters();
    Type[] actualTypeArguments = null;
    if (type instanceof ParameterizedType) {
      actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments(); 
    } else if (type instanceof GenericType) {
      actualTypeArguments = ((GenericType)type).getTypeArgs(); 
    } else {
      return null;
    }
    for (int i=0; i<typeParameters.length; i++) {
      String name = typeParameters[i].getName();
      Type typeArg = actualTypeArguments[i];
      typeArgs.put(name, typeArg);
    }
    String typeArgName = fieldType.toString();
    return typeArgs.get(typeArgName);
  }
}
