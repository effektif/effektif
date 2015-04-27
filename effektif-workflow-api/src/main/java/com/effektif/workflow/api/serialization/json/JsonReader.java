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


/** an abstraction that allows objects to read their internal 
 * state from a json source.
 * 
 * The goal is to support multiple 
 * json technologies like eg Jackson and MongoDB.
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
