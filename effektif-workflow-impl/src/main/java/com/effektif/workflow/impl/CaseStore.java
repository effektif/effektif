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
package com.effektif.workflow.impl;

import java.util.List;

import com.effektif.workflow.api.model.CaseId;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.task.Case;
import com.effektif.workflow.api.task.CaseQuery;


/**
 * @author Tom Baeyens
 */
public interface CaseStore {

  CaseId generateCaseId();

  void insertCase(Case caze);

  List<Case> findCases(CaseQuery caseQuery);

  void deleteCases(CaseQuery caseQuery);

  /** adds the task to the case and 
   * returns true if the case was found, the authenticated user 
   * has edit rights and if the db operation succeeded. */
  boolean addTask(CaseId caseId, TaskId taskId);
}
