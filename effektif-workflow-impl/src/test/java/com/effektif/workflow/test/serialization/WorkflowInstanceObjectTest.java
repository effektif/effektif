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

import java.io.StringReader;

import org.junit.BeforeClass;

import com.effektif.workflow.impl.json.DefaultJsonStreamMapper;


/**
 * @author Tom Baeyens
 */
public class WorkflowInstanceObjectTest extends WorkflowInstanceStreamTest {

  static DefaultJsonStreamMapper jsonObjectMapper = null;
  
  @BeforeClass
  public static void initialize() {
    jsonObjectMapper = new DefaultJsonStreamMapper();
  }

  @Override
  public <T> T serialize(T o) {
    String jsonString = jsonObjectMapper.write(o);
    System.out.println(jsonString);
    StringReader jsonReader = new StringReader(jsonString);
    return (T) jsonObjectMapper.read(jsonReader, o.getClass());
  }
}
