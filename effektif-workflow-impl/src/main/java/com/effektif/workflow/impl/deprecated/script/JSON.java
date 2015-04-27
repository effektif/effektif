/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.impl.deprecated.script;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 */
public class JSON {
  
  ObjectMapper objectMapper = new ObjectMapper();

  public String stringify(Object object) {
    if (object==null) {
      return "null";
    }
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      try {
        return object.toString();
      } catch (Exception e1) {
        return "sorry but your object is not stringifyable";
      }
    }
  }
}
