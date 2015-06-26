package com.effektif.workflow.impl.json.types;

import com.effektif.workflow.api.types.DataType;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.json.JsonReader;
import com.effektif.workflow.impl.json.JsonWriter;
import com.effektif.workflow.impl.json.TypeMapping;

/**
 * @author Christian Wiggert
 */
public class VariableMapper extends BeanMapper<Variable> {

  public VariableMapper(TypeMapping typeMapping) {
    super(typeMapping);
  }

  @Override
  public void write(Variable variable, JsonWriter jsonWriter) {
    DataType type = variable.getType();
    Object value = variable.getDefaultValue();
    if (type==null && value!=null) {
      type = mappings.getTypeByValue(value);
      variable.setType(type);
    }
    super.write(variable, jsonWriter);
  }

  @Override
  public Variable read(Object jsonValue, JsonReader jsonReader) {
    Variable variable = super.read(jsonValue, jsonReader);
    DataType type = variable.getType();
    Object jsonVariableValue = variable.getDefaultValue();
    if (jsonVariableValue!=null && type!=null) {
      Object objectVariableValue = jsonReader.readObject(jsonVariableValue, type.getValueType());
      variable.setDefaultValue(objectVariableValue);
    }
    return variable;
  }
}
