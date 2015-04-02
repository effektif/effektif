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
package com.effektif.workflow.api.json;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.Id;


/**
 * @author Tom Baeyens
 */
public interface JsonWriter {

  void writeString(String fieldName, String stringValue);
  
  void writeBoolean(String fieldName, Boolean value);
  
  void writeNumber(String fieldName, Number value);

  void writeObject(String fieldName, JsonWritable o);

  void writeId(Id id);

  void writeId(String fieldName, Id id);

  void writeDate(String fieldName, LocalDateTime value);

  void writeList(String fieldName, List<? extends Object> elements);

  void writeFields(Map<String,? extends Object> fieldValues);

  void writeMap(String fieldName, Map<String,? extends Object> map);
}
