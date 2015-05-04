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
import java.util.Arrays;
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

  /** converts ParameterizedType into GenericType */
  public static Type unify(Type type) {
    if (type==null
        || type instanceof Class
        || type instanceof GenericType) {
      return type;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (! (rawType instanceof Class)) {
        throw new RuntimeException("Type "+type+" has non-class raw type "+rawType);
      }
      Type[] typeArgs = null; 
      Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      if (actualTypeArguments!=null) {
        typeArgs = new Type[actualTypeArguments.length];
        for (int i=0; i<actualTypeArguments.length; i++) {
          typeArgs[i] = unify(actualTypeArguments[i]);
        }
      }
      return new GenericType((Class) rawType, typeArgs);
    }
    if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      Type[] upperBounds = wildcardType.getUpperBounds();
      if (upperBounds==null || upperBounds.length!=1) {
        throw new RuntimeException("Type"+type+" doesn't have single upperbound "+(upperBounds!=null ? Arrays.asList(upperBounds) : null));
      }
      return unify(upperBounds[0]);
    }
    throw new RuntimeException("Unknown type: "+type+" ("+type.getClass().getName()+")");
  }
  
  public static Class<?> getRawClass(Type type) {
    if (type==null) {
      return null;
    }
    if (type instanceof Class) {
      return (Class<?>)type;
    }
    if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType)type).getRawType();
    }
    if (type instanceof GenericType) {
      return ((GenericType)type).getRawClass();
    }
    if (type instanceof WildcardType) {
      return (Class) ((WildcardType)type).getUpperBounds()[0];
    }
    throw new RuntimeException("Unexpected type: "+type+" ("+type.getClass().getName()+"). Please perform GenericType.unify on the type first");
  }

  public static Type getSuperclass(Type type) {
    return getRawClass(type).getGenericSuperclass();
  }

  public static String getSimpleName(Type type) {
    if (type==null) {
      return "null";
    }
    if (type instanceof Class) {
      return ((Class)type).getSimpleName();
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      StringBuilder name = new StringBuilder();
      String rawname = getRawClass(parameterizedType.getRawType()).getSimpleName();
      name.append(rawname);
      Type[] typeArgs = parameterizedType.getActualTypeArguments();
      if (typeArgs!=null) {
        name.append("<");
        for (int i = 0; i < typeArgs.length; i++) {
          if (i!=0) {
            name.append(",");
          }
          name.append(getSimpleName(typeArgs[i]));
        }
        name.append(">");
      }
      return name.toString();
    }
    return type.toString();
  }

  public static Object instantiate(Class< ? > clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean isParameterized(Type type) {
    return type instanceof ParameterizedType
           || type instanceof GenericType;
  }

  public static Type[] getTypeArgs(Type type) {
    if (type instanceof ParameterizedType) {
      return ((ParameterizedType)type).getActualTypeArguments();
    }
    if (type instanceof GenericType) {
      return ((GenericType)type).getTypeArgs();
    }
    return null;
  }

  public static Map<TypeVariable, Type> getTypeArgsMap(Type type) {
    Type[] typeArgs = getTypeArgs(type);
    if (typeArgs==null) {
      return null;
    }
    Class<?> rawClass = getRawClass(type);
    Map<TypeVariable,Type> typeVariables = new HashMap<>();
    // Map<String,Type> typeArgsMap = new HashMap<>();
    TypeVariable<?>[] typeParameters = rawClass.getTypeParameters();
    for (int i=0; i<typeArgs.length; i++) {
      typeVariables.put(typeParameters[i], typeArgs[i]);
      // typeArgsMap.put(typeParameters[i].toString(), typeArgs[i]);
    }
    return typeVariables;
  }

  public static String getSimpleName(Field field) {
    return field.getDeclaringClass().getSimpleName()+"."+field.getName();
  }
}
