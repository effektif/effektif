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
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.mapper.JsonWritable;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;


/**
 * @author Tom Baeyens
 */
public class RestJsonWriter extends AbstractWriter {
  
  public static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime();

  JsonGenerator jgen;
  boolean pretty;
  Class<?> writableClass;

  public RestJsonWriter() {
  }

  public RestJsonWriter(Mappings mappings) {
    super(mappings);
  }

  public String toString(JsonWritable o) {
    StringWriter stringWriter = new StringWriter();
    toStream(o, stringWriter);
    return stringWriter.toString(); 
  }

  public String toStringPretty(JsonWritable o) {
    pretty = true;
    return toString(o); 
  }
  
  public RestJsonWriter pretty() {
    pretty = true;
    return this;
  }

  public void toStream(JsonWritable o, Writer writer) {
    try {
      jgen = new JsonFactory().createGenerator(writer);
      if (pretty) {
        jgen.setPrettyPrinter(new DefaultPrettyPrinter());
      }
      writeWritable(o);
      jgen.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
      if (o instanceof String) {
        jgen.writeString((String) o);
      } else if (o instanceof LocalDateTime) {
        writeDate((LocalDateTime) o);
      } else if (o instanceof Number) {
        writeNumber((Number) o, type);
      } else if (o instanceof Boolean) {
        jgen.writeBoolean((Boolean) o);
      } else if (o instanceof Id) {
        writeIdValue((Id) o);
      } else if (o instanceof JsonWritable) {
        writeWritable((JsonWritable)o);
      } else if (o instanceof Map) {
        ParameterizedType mapType = (ParameterizedType) type; 
        writeMap((Map) o, mapType.getActualTypeArguments()[1]);
      } else if (o instanceof List) {
        ParameterizedType mapType = (ParameterizedType) type; 
        writeList((List) o, mapType.getActualTypeArguments()[0]);
      } else if (o instanceof Binding) {
        ParameterizedType bindingType = (ParameterizedType) type; 
        writeBinding((Binding) o, bindingType.getActualTypeArguments()[0]);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeNumber(Number value, Type type) {
    try {
      if (value==null) {
        jgen.writeNull();
      } else if (Long.class.isAssignableFrom((Class<?>)type)) {
        jgen.writeNumber(value.longValue());
      } else if (Double.class.isAssignableFrom((Class<?>)type)) {
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
