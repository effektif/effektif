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

import java.lang.reflect.Type;
import java.util.List;

import com.effektif.workflow.impl.util.Reflection;


/**
 * @author Tom Baeyens
 */
public class TypeMapping {
  
  // private static final Logger log = LoggerFactory.getLogger(TypeMapping.class);

  Type type;
  Class<?> rawClass;
  List<FieldMapping> fieldMappings;
  
  public TypeMapping(Type type) {
    this.type = type;
    this.rawClass = Reflection.getRawClass(type);
  }
  
  public boolean isParameterized() {
    return type!=rawClass;
  }
  
  public Object instantiate() {
    return Reflection.instantiate(rawClass);
  }

  public Class< ? > getRawClass() {
    return rawClass;
  }
  
  public List<FieldMapping> getFieldMappings() {
    return fieldMappings;
  }

  public Type getType() {
    return type;
  }
  
  public String toString() {
    return "TypeMapping<"+Reflection.getSimpleName(type)+">";
  }

  public void setFieldMappings(List<FieldMapping> fieldMappings) {
    this.fieldMappings = fieldMappings;
  }
}
