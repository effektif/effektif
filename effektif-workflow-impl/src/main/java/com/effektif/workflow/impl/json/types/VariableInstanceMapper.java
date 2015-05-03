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

import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.TypeMapping;


/**
 * Maps a {@link String} to a JSON string field for serialisation and deserialisation.
 *
 * @author Tom Baeyens
 */
public class VariableInstanceMapper extends BeanMapper<VariableInstance> {

  public VariableInstanceMapper(TypeMapping typeMapping) {
    super(typeMapping);
  }

  @Override
  public void write(VariableInstance variableInstance, JsonWriter jsonWriter) {
    DataType type = variableInstance.getType();
    Object value = variableInstance.getValue();
    if (type==null && value!=null) {
      type = mappings.getTypeByValue(value);
      variableInstance.setType(type);
    }
    super.write(variableInstance, jsonWriter);
  }

  @Override
  public VariableInstance read(Object jsonValue, JsonReader jsonReader) {
    VariableInstance variableInstance = super.read(jsonValue, jsonReader);
    DataType type = variableInstance.getType();
    Object jsonVariableValue = variableInstance.getValue();
    if (jsonVariableValue!=null && type!=null) {
      Object objectVariableValue = jsonReader.readObject(jsonVariableValue, type.getValueType());
      variableInstance.setValue(objectVariableValue);
    }
    return variableInstance;
  }
}
