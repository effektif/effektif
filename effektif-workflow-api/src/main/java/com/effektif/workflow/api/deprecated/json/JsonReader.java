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
package com.effektif.workflow.api.deprecated.json;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.Id;
import com.effektif.workflow.api.workflow.Binding;


/**
 * An API for deserialising model objects from a JSON source. This API is an abstract interface so it can support
 * multiple concrete JSON implementations: Jackson for the REST APIs, and MongoDB.
 * 
 * @author Tom Baeyens
 */
public interface JsonReader {

  <T extends Id> T readId();
  <T extends Id> T readId(String fieldName);
  String readString(String fieldName);
  Boolean readBoolean(String fieldName);
  Long readLong(String fieldName);
  Double readDouble(String fieldName);
  LocalDateTime readDate(String fieldName);
  Class< ? > readClass(String fieldName);

  <T> T readObject(String fieldName);
  <T> Binding<T> readBinding(String fieldName);
  
  <T> List<T> readList(String fieldName);
  <T> Map<String, T> readMap(String fieldName);
  Map<String, Object> readProperties();
}
