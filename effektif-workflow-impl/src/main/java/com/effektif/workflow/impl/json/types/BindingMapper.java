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
package com.effektif.workflow.impl.json.types;

import java.lang.reflect.Type;

import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.TypeMapping;
import com.effektif.workflow.impl.util.Reflection;


/**
 * Maps a {@link String} to a JSON string field for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class BindingMapper extends BeanMapper<Binding> {
  
  boolean isParameterized = false;
  
  public BindingMapper(TypeMapping typeMapping) {
    super(typeMapping);
    if (typeMapping.isParameterized()) {
      Type typeArg = Reflection.getTypeArg(typeMapping.getType(), 0);
      if (typeArg!=null 
          && typeArg!=Object.class) {
        isParameterized = true;
      }
    }
  }
  
  @Override
  public void write(Binding typeValue, JsonWriter jsonWriter) {
    DataType dataType = typeValue.getType();
    Object value = typeValue.getValue();
    if (!isParameterized // if it's parameterized, then the this.typeMapping already performed the deserialization of the value
        && dataType==null 
        && value!=null) {
      dataType = mappings.getTypeByValue(value);
      typeValue.setType(dataType);
    }
    super.write(typeValue, jsonWriter);
  }

  @Override
  public Binding read(Object jsonValue, JsonReader jsonReader) {
    Binding binding = super.read(jsonValue, jsonReader);
    DataType dataType = binding.getType();
    Object jsonVariableValue = binding.getValue();
    if (!isParameterized // if it's parameterized, then the this.typeMapping already performed the deserialization of the value 
        && jsonVariableValue!=null 
        && dataType!=null) {
      Object objectVariableValue = jsonReader.readObject(jsonVariableValue, dataType.getValueType());
      binding.setValue(objectVariableValue);
    }
    return binding;
  }
}
