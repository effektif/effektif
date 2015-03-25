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
package com.effektif.workflow.impl.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.effektif.workflow.api.acl.Authentication;
import com.effektif.workflow.api.acl.Authentications;
import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.impl.email.EmailStore;
import com.effektif.workflow.impl.email.PersistentEmail;


/**
 * In-memory {@link java.util.Map}-based email storage implementation.
 *
 * @author Tom Baeyens
 */
public class MemoryEmailStore implements EmailStore {
  
  Map<EmailId, PersistentEmail> emails = new ConcurrentHashMap<>();
  long nextId = 1;

  @Override
  public void insertEmail(PersistentEmail email) {
    Authentication authentication = Authentications.current();
    String organizationId = authentication!=null ? authentication.getOrganizationId() : null;
    if (organizationId!=null) {
      email.setOrganizationId(organizationId);
    }

    EmailId emailId = new EmailId(Long.toString(nextId++));
    email.setId(emailId);
    emails.put(emailId, email);
  }

  @Override
  public PersistentEmail findEmailById(EmailId emailId) {
    return emails.get(emailId);
  }
}
