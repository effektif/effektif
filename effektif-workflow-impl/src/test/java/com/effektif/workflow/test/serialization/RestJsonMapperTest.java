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

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.effektif.workflow.api.activities.Call;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.impl.mapper.JsonMapper;


/**
 * @author Tom Baeyens
 */
public class RestJsonMapperTest extends AbstractMapperTest {

  static JsonMapper jsonMapper = new JsonMapper();
  
  @BeforeClass
  public static void initialize() {
    initializeMappings();
    jsonMapper = new JsonMapper();
    jsonMapper.setMappings(mappings);
  }
  
  @Override
  protected <T> T serialize(T o) {
    String jsonString = jsonMapper
      .writeToStringPretty(o);
    
    System.out.println(jsonString);
    
    return (T) jsonMapper
      .readFromString(jsonString, o.getClass());
  }
  
  // NOT READY TO MOVE TO ABSTRACT MAPPER TEST
  @Test
  public void testCall() {
    Call activity = new Call()
      .id("runTests")
      .subWorkflowName("Run tests")
      .subWorkflowId(new WorkflowId("551d4f5803649532d21f223f"));
    activity.setSubWorkflowSource("releaseTests");
    
    activity = serialize(activity);
    
    assertEquals(new WorkflowId("551d4f5803649532d21f223f"), activity.getSubWorkflowId());
    assertEquals("releaseTests", activity.getSubWorkflowSource());
  }
}
