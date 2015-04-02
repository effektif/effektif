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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.effektif.workflow.api.json.JsonWritable;
import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.impl.json.deprecated.JsonMappings;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;


/**
 * @author Tom Baeyens
 */
public class RestJsonWriter extends AbstractJsonWriter {
  
  public static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime();

  JsonGenerator jgen;

  public RestJsonWriter() {
  }

  public RestJsonWriter(JsonMappings jsonMappings) {
    super(jsonMappings);
  }

  public void toStream(JsonWritable o, Writer writer) {
    try {
      jgen = new JsonFactory().createGenerator(writer);
      writeObject(o);
      jgen.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String toString(JsonWritable o) {
    StringWriter stringWriter = new StringWriter();
    toStream(o, stringWriter);
    return stringWriter.toString(); 
  }

  protected void writeNumber(Number value) {
    try {
      if (value==null) {
        jgen.writeNull();
      } else if (value instanceof Long) {
        jgen.writeNumber(value.longValue());
      } else if (value instanceof Double) {
        jgen.writeNumber(value.doubleValue());
      } else if (value instanceof BigDecimal) {
        jgen.writeNumber((BigDecimal) value);
      } else {
        throw new RuntimeException("Unknown number "+value+" ("+value.getClass().getName()+")");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeDate(LocalDateTime value) {
    try {
      jgen.writeString(DATE_FORMAT.print(value));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeObject(Object o) {
    try {
      if (o instanceof String) {
        jgen.writeString((String) o);
      } else if (o instanceof LocalDateTime) {
        writeDate((LocalDateTime) o);
      } else if (o instanceof Number) {
        writeNumber((Number) o);
      } else if (o instanceof Boolean) {
        jgen.writeBoolean((Boolean) o);
      } else if (o instanceof JsonWritable) {
        writeObject((JsonWritable)o);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeObject(JsonWritable o) {
    try {
      if (o!=null) {
        jgen.writeStartObject();
        jsonMappings.writeTypeField(this, o);
        o.writeFields(this);
        jgen.writeEndObject();
      } else {
        jgen.writeNull();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
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
  public void writeNumber(String fieldName, Number value) {
    if (value!=null) {
      try {
        jgen.writeFieldName(fieldName);
        writeNumber(value);
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
  public void writeId(Id id) {
    writeId("id", id);
  }
  
  @Override
  public void writeId(String fieldName, Id id) {
    if (id!=null) {
      try {
        jgen.writeStringField(fieldName, id.getInternal());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeObject(String fieldName, JsonWritable o) {
    if (o!=null) {
      try {
        jgen.writeFieldName(fieldName);
        writeObject(o);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeList(String fieldName, List<? extends Object> elements) {
    if (elements!=null) {
      try {
        jgen.writeFieldName(fieldName);
        jgen.writeStartArray();
        for (Object element: elements) {
          writeObject(element);
        }
        jgen.writeEndArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeFields(Map<String,? extends Object> map) {
    if (map!=null) {
      for (String key: map.keySet()) {
        Object value = map.get(key);
        if (value!=null) {
          try {
            jgen.writeFieldName(key);
            writeObject(value);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }
  
  @Override
  public void writeMap(String fieldName, Map<String,? extends Object> map) {
    if (map!=null) {
      try {
        jgen.writeFieldName(fieldName);
        jgen.writeStartObject();
        writeFields(map);
        jgen.writeEndObject();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
