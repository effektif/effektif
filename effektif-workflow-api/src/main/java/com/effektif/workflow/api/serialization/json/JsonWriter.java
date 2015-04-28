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
package com.effektif.workflow.api.serialization.json;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;


/**
 * An API for serialising model objects to JSON. This API is an abstract interface so it can support multiple concrete
 * JSON implementations: Jackson for the REST APIs, and MongoDB.
 *
 * @author Tom Baeyens
 */
public interface JsonWriter {

  void writeId(Id id);
  void writeId(String fieldName, Id value);
  void writeString(String fieldName, String value);
  void writeBoolean(String fieldName, Boolean value);
  void writeLong(String fieldName, Long value);
  void writeDouble(String fieldName, Double value);
  void writeDate(String fieldName, LocalDateTime value);
  void writeClass(String fieldName, Class< ? > value);

  void writeWritable(String fieldName, JsonWritable value);
  <T> void writeBinding(String fieldName, Binding<T> value);

  void writeList(String fieldName, List<?> value);
  void writeMap(String fieldName, Map<String,?> value);
  void writeProperties(Map<String, Object> value);
}
