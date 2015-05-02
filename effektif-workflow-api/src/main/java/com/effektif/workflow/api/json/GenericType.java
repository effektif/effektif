/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.api.json;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;


/**
 * A container for type information that is used by JSON deserialisation.
 *
 * @author Tom Baeyens
 */
public class GenericType implements Type {
  
  public static final Type UNKNOWN_TYPE = new Type() {};
  
  protected Class<?> rawClass;
  protected Type[] typeArgs;
  
  public GenericType(Class< ? > rawClass, Type... typeArgs) {
    this.rawClass = rawClass;
    this.typeArgs = typeArgs;
  }

  public Class<?> getRawClass() {
    return this.rawClass;
  }
  
  public Type[] getTypeArgs() {
    return this.typeArgs;
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
    if (type instanceof GenericType) {
      return ((GenericType)type).getRawClass();
    }
    throw new RuntimeException("Unexpected type: "+type+" ("+type.getClass().getName()+"). Please perform GenericType.unify on the type first");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rawClass == null) ? 0 : rawClass.hashCode());
    result = prime * result + Arrays.hashCode(typeArgs);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GenericType other = (GenericType) obj;
    if (rawClass == null) {
      if (other.rawClass != null)
        return false;
    } else if (!rawClass.equals(other.rawClass))
      return false;
    if (!Arrays.equals(typeArgs, other.typeArgs))
      return false;
    return true;
  }

  @Override
  public String toString() {
    if (typeArgs==null) {
      return rawClass.getSimpleName();
    } else {
      return rawClass.getSimpleName()+Arrays.asList(typeArgs);
    }
  }

  public static Type getSuperclass(Type type) {
    return unify(getRawClass(type).getGenericSuperclass());
  }

  public static String getSimpleName(Type type) {
    Class< ? > clazz = getRawClass(type);
    return clazz!=null ? clazz.getSimpleName() : null;
  }
}
