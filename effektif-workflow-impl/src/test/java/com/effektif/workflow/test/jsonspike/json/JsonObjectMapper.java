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
package com.effektif.workflow.test.jsonspike.json;

import java.util.Map;

import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.test.jsonspike.json.typemappers.LocalDateTimeDateMapper;


/**
 * @author Tom Baeyens
 */
public class JsonObjectMapper {

  Mappings mappings;
  
  public JsonObjectMapper() {
    this.mappings = new Mappings();
    
    this.mappings.registerTypeMapper(new LocalDateTimeDateMapper());
    
    this.mappings.findFieldMapping(Workflow.class, "id").setJsonFieldName("_id");
  }

  public <T> T read(Map<String,Object> beanJsonMap, Class<?> clazz) {
    JsonObjectFieldReader jsonObjectFieldReader = new JsonObjectFieldReader(mappings);
    return (T) jsonObjectFieldReader.readBean(beanJsonMap, clazz);
  }
  
  public <T> Map<String,Object> write(T bean) {
    JsonObjectFieldWriter jsonObjectFieldWriter = new JsonObjectFieldWriter(mappings);
    jsonObjectFieldWriter.writeObject(bean);
    return (Map<String, Object>) jsonObjectFieldWriter.result;
  }
}
