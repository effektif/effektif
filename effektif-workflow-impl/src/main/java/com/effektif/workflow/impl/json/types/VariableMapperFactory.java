package com.effektif.workflow.impl.json.types;

import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.impl.json.JsonTypeMapper;
import com.effektif.workflow.impl.json.JsonTypeMapperFactory;
import com.effektif.workflow.impl.json.Mappings;
import com.effektif.workflow.impl.json.TypeMapping;

import java.lang.reflect.Type;

/**
 * @author Christian Wiggert
 */
public class VariableMapperFactory implements JsonTypeMapperFactory {

  @Override
  public JsonTypeMapper createTypeMapper(Type type, Class<?> clazz, Mappings mappings) {
    if (clazz==Variable.class) {
      TypeMapping typeMapping = mappings.getTypeMapping(type);
      return new VariableMapper(typeMapping);
    }
    return null;
  }
}
