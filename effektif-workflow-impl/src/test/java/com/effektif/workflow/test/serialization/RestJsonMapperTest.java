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
package com.effektif.workflow.test.serialization;

import org.junit.BeforeClass;

import com.effektif.workflow.api.mapper.Readable;
import com.effektif.workflow.api.mapper.Writable;
import com.effektif.workflow.impl.mapper.RestJsonMapper;


/**
 * @author Tom Baeyens
 */
public class RestJsonMapperTest extends AbstractMapperTest {

  static RestJsonMapper restJsonMapper = new RestJsonMapper();
  
  @BeforeClass
  public static void initialize() {
    initializeMappings();
    restJsonMapper = new RestJsonMapper();
    restJsonMapper.setJsonMappings(mappings);
  }
  
  @Override
  protected <T extends Readable> T serialize(T o) {
    String jsonString = restJsonMapper
      .createWriter()
      .toString((Writable)o);
    
    System.out.println(jsonString);
    
    return (T) restJsonMapper
      .createReader()
      .toObject(jsonString, (Class<Readable>) o.getClass());
  }
}
