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

import com.effektif.workflow.api.workflow.Activity;


/**
 * @author Tom Baeyens
 */
public class RestJsonService {
  
  SubclassMappings subclassMappings = new SubclassMappings(); 

  public void registerBaseClass(Class<Activity> baseClass) {
    subclassMappings.registerBaseClass(baseClass);
  }

  public void registerBaseClass(Class<Activity> baseClass, String typeField) {
    subclassMappings.registerBaseClass(baseClass, typeField);
  }

  public void registerSubClass(Class<?> subClass) {
    subclassMappings.registerSubClass(subClass);
  }

  public RestJsonReader createJsonReader() {
    return new RestJsonReader(subclassMappings);
  }

  public RestJsonWriter createJsonWriter() {
    return new RestJsonWriter(subclassMappings);
  }
}
