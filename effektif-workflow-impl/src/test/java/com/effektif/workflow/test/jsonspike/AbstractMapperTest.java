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
package com.effektif.workflow.test.jsonspike;

import static org.junit.Assert.*;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.effektif.workflow.api.workflow.Workflow;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractMapperTest {
  
  @Test 
  public void testSimpleObjectBasicProperties() {
    LocalDateTime now = new LocalDateTime();

    Workflow workflow = new Workflow();
    workflow.name("w");
    workflow.createTime(now);
    
    workflow = serialize(workflow);
    
    assertNotNull(workflow);
    assertEquals("w", workflow.getName());
    assertEquals(now, workflow.getCreateTime());
  }

  public abstract <T> T serialize(T o);
}
