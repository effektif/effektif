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
package com.effektif.workflow.impl.plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.type.BindingType;
import com.effektif.workflow.api.type.ListType;
import com.effektif.workflow.api.type.ObjectField;
import com.effektif.workflow.api.type.ObjectType;
import com.effektif.workflow.api.type.Type;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.WorkflowEngineImpl;

@Deprecated // not sure if this should be kept... just to make sure i don't leave it in by accident
public abstract class Serializer {
  
  public abstract void serialize(Object o);
  public abstract boolean isEmpty();

  public static Serializer createSerializer(Type type, WorkflowEngineImpl workflowEngine) {
    if (type instanceof ObjectType) {
      ObjectSerializer objectSerializer = new ObjectSerializer((ObjectType) type, workflowEngine);
      return !objectSerializer.isEmpty() ? objectSerializer : null;
    } else if (type instanceof ListType) {
      ListSerializer listSerializer = new ListSerializer((ListType) type, workflowEngine);
      return !listSerializer.isEmpty() ? listSerializer : null;
    } else if (type instanceof BindingType) {
      return new BindingSerializer(workflowEngine);
    }
    return null;
  }

  public static class BindingSerializer extends Serializer {
    PluginService pluginService;
    public BindingSerializer(WorkflowEngineImpl workflowEngine) {
      this.pluginService = workflowEngine.getServiceRegistry().getService(PluginService.class);
    }
    @Override
    public void serialize(Object o) {
      Binding binding = (Binding) o;
      Object value = binding.getValue();
      if (value!=null && binding.getType()==null) {
        DataType dataType = pluginService.getDataTypeByValueClass(value.getClass());
        if (dataType==null) {
          throw new RuntimeException("No data type found for value "+value+" ("+value.getClass().getName()+")");
        }
        binding.type(dataType.getApiType());
      }
    }
    @Override
    public boolean isEmpty() {
      return false;
    }
  }

  public static class ObjectSerializer extends Serializer {
    List<FieldSerializer> fieldSerializers = new ArrayList<>();
    public ObjectSerializer(ObjectType objectType, WorkflowEngineImpl workflowEngine) {
      if (objectType.getFields()!=null) {
        for (ObjectField field: objectType.getFields()) {
          Serializer serializer = createSerializer(field.getType(), workflowEngine);
          if (serializer!=null)  {
            String fieldName = field.getName();
            FieldSerializer fieldSerializer = new FieldSerializer();
            try {
              fieldSerializer.field = objectType.getApiClass().getField(fieldName);
              fieldSerializer.field.setAccessible(true);
              fieldSerializer.serializer = serializer;
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            fieldSerializers.add(fieldSerializer);
          }
        }
      }
    }
    public boolean isEmpty() {
      return fieldSerializers.isEmpty();
    }
    @Override
    public void serialize(Object o) {
      for (FieldSerializer fieldSerializer: fieldSerializers) {
        fieldSerializer.serialize(o);
      }
    }
  }

  public static class FieldSerializer {
    Field field;
    Serializer serializer;
    public void serialize(Object o) {
      try {
        Object fieldValue = field.get(o);
        serializer.serialize(fieldValue);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static class ListSerializer extends Serializer {
    Serializer elementSerializer;
    public ListSerializer(ListType type, WorkflowEngineImpl workflowEngine) {
    }
    public boolean isEmpty() {
      return elementSerializer.isEmpty();
    }
    @Override
    public void serialize(Object o) {
      
    }
  }
}
