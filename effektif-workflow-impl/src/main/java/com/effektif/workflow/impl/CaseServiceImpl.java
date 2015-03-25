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

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.acl.Access;
import com.effektif.workflow.api.acl.AccessControlList;
import com.effektif.workflow.api.acl.Authentication;
import com.effektif.workflow.api.acl.Authentications;
import com.effektif.workflow.api.model.CaseId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.task.Case;
import com.effektif.workflow.api.task.CaseQuery;
import com.effektif.workflow.api.task.CaseService;
import com.effektif.workflow.impl.configuration.Brewable;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.exceptions.BadRequestException;
import com.effektif.workflow.impl.util.Time;


/**
 * @author Tom Baeyens
 */
public class CaseServiceImpl implements CaseService, Brewable {
  
  protected CaseStore caseStore;
  protected NotificationService notificationService;
  protected WorkflowEngineImpl workflowEngine;

  @Override
  public void brew(Brewery brewery) {
    this.caseStore = brewery.get(CaseStore.class);
    this.notificationService = brewery.getOpt(NotificationService.class);
    this.workflowEngine = brewery.get(WorkflowEngineImpl.class);
  }

  @Override
  public Case createCase(Case caze) {
    if (caze==null) {
      caze = new Case();
    }
    
    Authentication authentication = Authentications.current();
    String organizationId = authentication!=null ? authentication.getOrganizationId() : null;
    String actorId = authentication!=null ? authentication.getUserId() : null;
    UserId actorUserId = actorId!=null ? new UserId(actorId) : null;

    CaseId caseId = caze.getId();
    if (caseId==null) {
      caseId = caseStore.generateCaseId();
    }
    
    caze.setId(caseId);
    caze.setOrganizationId(organizationId);
    caze.setCreatorId(actorUserId);
    caze.setCreateTime(Time.now());
    
    List<UserId> participants = caze.getParticipantIds();
    if (actorUserId!=null) {
      if (participants==null) {
        participants = new ArrayList<>();
      }
      // we want to add the actor if not already present
      // and we want the creator to be the first in the list
      if (participants.contains(actorUserId)) {
        participants.remove(actorUserId);
      }
      participants.add(0, actorUserId);
    }
    caze.setParticipantIds(participants);

    AccessControlList access = caze.getAccess();
    // if access is specified 
    // and the current authenticated user does not have access 
    if ( access!=null // if access is specified  
         && ! ( access.hasPermission(authentication, Access.EDIT)
                && access.hasPermission(authentication, Access.VIEW) 
              )
       ) {
      throw new BadRequestException("If you specify access control, the creator must at least have view and edit access");
    }
    caze.setAccess(access);
    
    caze.setLastUpdated(Time.now());

    caseStore.insertCase(caze);

    if (notificationService!=null) {
      notificationService.caseCreated(caze);
    }
    
    return caze;
  }

  public Case findCaseById(String caseId) {
    List<Case> cases = findCases(new CaseQuery().caseId(caseId));
    return !cases.isEmpty() ? cases.get(0) : null;
  }

  @Override
  public List<Case> findCases(CaseQuery caseQuery) {
    return caseStore.findCases(caseQuery);
  }

  @Override
  public void deleteCases(CaseQuery caseQuery) {
    caseStore.deleteCases(caseQuery);
  }
}
