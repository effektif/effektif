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

import java.lang.reflect.Type;
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
    StringBuilder name = new StringBuilder();
    name.append(rawClass.getSimpleName());
    if (typeArgs!=null) {
      name.append("<");
      for (int i = 0; i < typeArgs.length; i++) {
        if (i!=0) {
          name.append(",");
        }
        if (typeArgs[i] instanceof Class) {
          name.append(((Class)typeArgs[i]).getSimpleName());
        } else {
          name.append(typeArgs[i].toString());
        }
      }
      name.append(">");
    }
    return name.toString();
  }
}
