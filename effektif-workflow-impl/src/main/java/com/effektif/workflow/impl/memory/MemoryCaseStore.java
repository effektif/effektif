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
package com.effektif.workflow.impl.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.task.Case;
import com.effektif.workflow.api.task.CaseId;
import com.effektif.workflow.api.task.CaseQuery;
import com.effektif.workflow.impl.CaseStore;


/**
 * @author Tom Baeyens
 */
public class MemoryCaseStore implements CaseStore {
  
  Map<CaseId, Case> cases = new ConcurrentHashMap<>();
  long nextId = 1;

  @Override
  public CaseId generateCaseId() {
    return new CaseId(Long.toString(nextId++));
  }

  @Override
  public void insertCase(Case caze) {
    cases.put(caze.getId(), caze);
  }

  @Override
  public List<Case> findCases(CaseQuery caseQuery) {
    List<Case> result = new ArrayList<>();
    for (Case caze: cases.values()) {
      if (caseQuery.meetsCriteria(caze)) {
        result.add(caze);
      }
    }
    return result;
  }

  @Override
  public void deleteCases(CaseQuery caseQuery) {
    for (Case caze: findCases(caseQuery)) {
      cases.remove(caze.getId());
    }
  }
}
