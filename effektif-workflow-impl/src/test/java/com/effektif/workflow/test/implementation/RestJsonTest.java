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
package com.effektif.workflow.test.implementation;

import org.junit.BeforeClass;

import com.effektif.workflow.api.json.JsonReadable;
import com.effektif.workflow.api.json.JsonWritable;
import com.effektif.workflow.impl.json.RestJsonService;


/**
 * @author Tom Baeyens
 */
public class RestJsonTest extends AbstractJsonTest {

  static RestJsonService restJsonService = new RestJsonService();
  
  @BeforeClass
  public static void initialize() {
    initializeSubclassMappings();
    restJsonService = new RestJsonService();
    restJsonService.setJsonMappings(jsonMappings);
  }
  
  @Override
  protected <T extends JsonReadable> T serialize(T o) {
    String jsonString = restJsonService
      .createJsonWriter()
      .toString((JsonWritable)o);
    
    System.out.println(jsonString);
    
    return (T) restJsonService
      .createJsonReader()
      .toObject(jsonString, (Class<JsonReadable>) o.getClass());
  }
}
