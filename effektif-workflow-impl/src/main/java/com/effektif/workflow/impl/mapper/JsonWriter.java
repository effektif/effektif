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
package com.effektif.workflow.impl.mapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.core.JsonGenerator;


/**
 * @author Tom Baeyens
 */
public class JsonWriter extends AbstractWriter {
  
  public static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime();

  JsonGenerator jgen;
  Class<?> writableClass;

  public JsonWriter() {
  }

  public JsonWriter(Mappings mappings, JsonGenerator jgen) {
    super(mappings);
    this.jgen = jgen;
  }

  @Override
  public void writeId(Id id) {
    writeId("id", id);
  }
  
  @Override
  public void writeId(String fieldName, Id id) {
    if (id!=null) {
      try {
        jgen.writeFieldName(fieldName);
        writeIdValue(id);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeString(String fieldName, String value) {
    if (value!=null) {
      try {
        jgen.writeFieldName(fieldName);
        jgen.writeString(value);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  @Override
  public void writeBoolean(String fieldName, Boolean value) {
    if (value!=null) {
      try {
        jgen.writeBooleanField(fieldName, value);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  

  @Override
  public void writeLong(String fieldName, Long value) {
    if (value!=null) {
      try {
        jgen.writeFieldName(fieldName);
        jgen.writeNumber(value);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeDouble(String fieldName, Double value) {
    if (value!=null) {
      try {
        jgen.writeFieldName(fieldName);
        jgen.writeNumber(value);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeDate(String fieldName, LocalDateTime value) {
    if (value!=null) {
      try {
        jgen.writeFieldName(fieldName);
        writeDate(value);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  @Override
  public void writeClass(String fieldName, Class< ? > value) {
    if (value!=null) {
      try {
        jgen.writeFieldName(fieldName);
        jgen.writeString(value.getName());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  @Override
  public void writeWritable(String fieldName, JsonWritable o) {
    if (o!=null) {
      try {
        jgen.writeFieldName(fieldName);
        writeWritable(o);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  @Override
  public void writeMap(String fieldName, Map<String,?> map) {
    if (map!=null) {
      try {
        ParameterizedType type = (ParameterizedType) mappings.getFieldType(writableClass, fieldName);
        Type valueType = type.getActualTypeArguments()[1];
        jgen.writeFieldName(fieldName);
        writeMap(map, valueType);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeList(String fieldName, List<?> elements) {
    if (elements!=null) {
      try {
        ParameterizedType type = (ParameterizedType) mappings.getFieldType(writableClass, fieldName);
        jgen.writeFieldName(fieldName);
        writeList(elements, type.getActualTypeArguments()[0]);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public <T> void writeBinding(String fieldName, Binding<T> binding) {
    if (binding!=null) {
      try {
        ParameterizedType type = (ParameterizedType) mappings.getFieldType(writableClass, fieldName);
        jgen.writeFieldName(fieldName);
        writeBinding(binding, type.getActualTypeArguments()[0]);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeProperties(Map<String, Object> properties) {
    writeMapFields(properties, null);
  }

  public <T> void writeBinding(Binding<T> binding, Type valueType) {
    if (binding==null) {
      return;
    }
    try {
      jgen.writeStartObject();
      if (binding.getValue()!=null) {
        jgen.writeFieldName("value");
        writeObject(binding.getValue(), valueType);
      }
      if (binding.getExpression()!=null) {
        jgen.writeStringField("expression", binding.getExpression());
      }
      
      jgen.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void writeObject(Object o, Type type) {
    if (o==null) {
      return;
    }
    try {
      if (is(String.class, type) || o instanceof String) {
        jgen.writeString((String) o);
      } else if (is(LocalDateTime.class, type) || o instanceof LocalDateTime) {
        writeDate((LocalDateTime) o);
      } else if (is(Number.class, type) || o instanceof Number) {
        writeNumber((Number) o, type);
      } else if (Boolean.class==type || o instanceof Boolean) {
        jgen.writeBoolean((Boolean) o);
      } else if (is(Id.class, type) || o instanceof Id) {
        writeIdValue((Id) o);
      } else if (is(JsonWritable.class, type) || o instanceof JsonWritable) {
        writeWritable((JsonWritable)o);
      } else if (is(List.class, type) || o instanceof List) {
        Type elementType = getTypeArg(type, 0);
        writeList((List) o, elementType);
      } else if (is(Map.class, type) || o instanceof Map) {
        Type valueType = getTypeArg(type, 1);
        writeMap((Map) o, valueType);
      } else if (is(Binding.class, type) || o instanceof Binding) {
        Type valueType = getTypeArg(type, 0);
        writeBinding((Binding) o, valueType);
      } else if (is(Class.class, type) || o instanceof Class) {
        jgen.writeString(((Class)o).getName());
      } else if (o.getClass().isEnum()) {
        jgen.writeString(o.toString());
      } else if (o.getClass().isArray()) {
        List<Object> list = Arrays.asList((Object[])o);
        Class< ? > elementType = o.getClass().getComponentType();
        writeList(list, elementType);
      } else {
        writeObjectDefault(o, type);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static boolean is(Class<?> baseType, Type type) {
    return type!=null 
           && ( ( type instanceof Class
                  && (baseType.isAssignableFrom((Class<?>)type)) 
                ) || ( type instanceof ParameterizedType 
                       && is(baseType, ((ParameterizedType)type).getRawType())
                     )
                  || ( type instanceof WildcardType 
                       && is(baseType, ((WildcardType)type).getUpperBounds()[0])
                     )
              );
  }

  protected Type getTypeArg(Type type, int index) {
    if (type==null || ! (type instanceof ParameterizedType)) {
      return null;
    }
    ParameterizedType mapType = (ParameterizedType) type; 
    Type valueType = mapType.getActualTypeArguments()[index];
    return valueType;
  }

  public void writeWritable(JsonWritable o) {
    try {
      if (o!=null) {
        Class<?> parentWritableClass = writableClass;
        writableClass = o.getClass();
        jgen.writeStartObject();
        mappings.writeTypeField(this, o);
        o.writeJson(this);
        jgen.writeEndObject();
        writableClass = parentWritableClass;
      } else {
        jgen.writeNull();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeObjectDefault(Object o, Type type) {
    try {
      if (o!=null) {
        Class<?> parentWritableClass = writableClass;
        writableClass = o.getClass();
        jgen.writeStartObject();
        mappings.writeTypeField(this, o);
        writeFields(o, writableClass);
        jgen.writeEndObject();
        writableClass = parentWritableClass;
      } else {
        jgen.writeNull();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeFields(Object o, Class<?> clazz) {
    List<Field> fields = mappings.getAllFields(clazz);
    if (fields!=null) {
      for (Field field : fields) {
        writeField(o, field);
      }
    }
  }
  
  public void writeField(Object o, Field field) {
    try {
      Object fieldValue = field.get(o);
      if (fieldValue!=null) {
        // log.debug("Writing field "+field);
        jgen.writeFieldName(field.getName());
        writeObject(fieldValue, field.getGenericType());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void writeNumber(Number value, Type type) {
    try {
      if (value==null) {
        jgen.writeNull();
      } else if (value instanceof Long 
                 || value instanceof Integer
                 || value instanceof Short
                 || value instanceof BigInteger) {
        jgen.writeNumber(value.longValue());
      } else if (value instanceof Double
                 || value instanceof Float
                 || value instanceof BigDecimal) {
        jgen.writeNumber(value.doubleValue());
      } else {
        throw new RuntimeException("Unknown number "+value+" ("+value.getClass().getName()+")");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeDate(LocalDateTime value) {
    try {
      jgen.writeString(DATE_FORMAT.print(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeIdValue(Id id) {
    try {
      if (id==null) {
        jgen.writeNull();
      } else {
        jgen.writeString(id.getInternal());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeMap(Map<String,?> map, Type valueType) {
    try {
      jgen.writeStartObject();
      writeMapFields(map, valueType);
      jgen.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeMapFields(Map<String,?> map, Type valueType) {
    if (map!=null) {
      for (String key: map.keySet()) {
        Object value = map.get(key);
        if (value!=null) {
          try {
            jgen.writeFieldName(key);
            writeObject(value, valueType);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }
  
  public void writeList(List< ? > elements, Type elementType) {
    try {
      jgen.writeStartArray();
      for (Object element: elements) {
        writeObject(element, elementType);
      }
      jgen.writeEndArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
