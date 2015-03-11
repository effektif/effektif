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
package com.effektif.workflow.api.task;

import com.effektif.workflow.api.model.CaseId;
import com.effektif.workflow.api.query.OrderDirection;
import com.effektif.workflow.api.query.Query;


/**
 * @author Tom Baeyens
 */
public class CaseQuery extends Query {

  protected CaseId caseId;

  public boolean meetsCriteria(Case caze) {
    if (caseId!=null && !caseId.equals(caze.getId())) {
      return false;
    }
    return true;
  }

  public CaseId getCaseId() {
    return this.caseId;
  }
  public void setCaseId(CaseId caseId) {
    this.caseId = caseId;
  }
  public CaseQuery caseId(CaseId caseId) {
    this.caseId = caseId;
    return this;
  }
  public CaseQuery caseId(String caseId) {
    this.caseId = new CaseId(caseId);
    return this;
  }

  @Override
  public CaseQuery skip(Integer skip) {
    super.skip(skip);
    return this;
  }

  @Override
  public CaseQuery limit(Integer limit) {
    super.limit(limit);
    return this;
  }

  @Override
  public CaseQuery orderBy(String field, OrderDirection direction) {
    super.orderBy(field, direction);
    return this;
  }
}