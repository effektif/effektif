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
package com.effektif.workflow.test.api;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.effektif.workflow.api.task.Case;
import com.effektif.workflow.api.task.CaseQuery;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class CaseTest extends WorkflowTest {

  @Test
  public void testCaseCrud() {
    Case caze = caseService.createCase(new Case()
      .name("hello"));
    
    assertNotNull(caze.getId());
    assertNotNull(caze.getId().getInternal());
    assertEquals("hello", caze.getName());
    assertNotNull(caze.getCreateTime());
    
    List<Case> cases = caseService.findCases(new CaseQuery().caseId(caze.getId()));
    assertTrue(!cases.isEmpty());
    caze = cases.get(0);
    assertNotNull(caze.getId());
    assertNotNull(caze.getId().getInternal());
    assertEquals("hello", caze.getName());
    assertNotNull(caze.getCreateTime());
    assertEquals(1, cases.size());

    caseService.deleteCases(new CaseQuery().caseId(caze.getId()));
    cases = caseService.findCases(new CaseQuery().caseId(caze.getId()));
    assertEquals(0, cases.size()); 
  }
}
