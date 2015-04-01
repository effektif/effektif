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
package com.effektif.workflow.impl.json;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.json.JsonWritable;
import com.effektif.workflow.api.json.JsonWriter;
import com.effektif.workflow.api.json.TypeName;


/**
 * @author Tom Baeyens
 */
public class SubclassMappings {

  Map<Class<?>, SubclassMapping> subclassMappings = new HashMap<>();
  Map<Class<?>, TypeField> typeFields = new HashMap<>();

  public void registerBaseClass(Class<?> baseClass) {
    registerBaseClass(baseClass, "type");
  }

  public void registerBaseClass(Class<?> baseClass, String typeField) {
    SubclassMapping subclassMapping = new SubclassMapping(typeField);
    subclassMappings.put(baseClass, subclassMapping);
  }

  public void registerSubClass(Class<?> subClass) {
    TypeName typeName = subClass.getAnnotation(TypeName.class);
    if (typeName==null) {
      throw new RuntimeException(subClass.getName()+" must declare "+TypeName.class.toString());
    }
    registerSubClass(subClass, typeName.value(), subClass);
  }

  public void registerSubClass(Class<?> baseClass, String typeName, Class<?> subClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    if (subclassMapping!=null) {
      subclassMapping.registerSubclass(typeName, subClass);
      typeFields.put(subClass, new TypeField(subclassMapping.getTypeField(), typeName));
    }
    Class< ? > superClass = baseClass.getSuperclass();
    if (superClass!=null) {
      registerSubClass(superClass, typeName, subClass);
    }
    for (Class<?> i: baseClass.getInterfaces()) {
      registerSubClass(i, typeName, subClass);
    }
  }

  public <T> Class<T> getConcreteClass(Map<String,Object> jsonObject, Class<T> baseClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    return subclassMapping!=null ? (Class<T>) subclassMapping.getSubclass(jsonObject) : baseClass;
  }

  public void writeTypeField(JsonWriter jsonWriter, JsonWritable o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      jsonWriter.writeString(typeField.getTypeField(), typeField.getTypeName());
    }
  } 
}
