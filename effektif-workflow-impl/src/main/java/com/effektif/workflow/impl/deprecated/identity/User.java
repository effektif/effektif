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
package com.effektif.workflow.impl.deprecated.identity;

import com.effektif.workflow.api.deprecated.model.UserId;


/**
 * @author Tom Baeyens
 */
public class User {

  protected UserId id;

  public UserId getId() {
    return this.id;
  }
  public void setId(UserId id) {
    this.id = id;
  }
  public User id(UserId id) {
    this.id = id;
    return this;
  }
  public User id(String id) {
    this.id = new UserId(id);
    return this;
  }
  
  protected String fullName;

  public String getFullName() {
    return this.fullName;
  }
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }
  public User fullName(String fullName) {
    this.fullName = fullName;
    return this;
  }
  
  protected String email;

  public String getEmail() {
    return this.email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public User email(String email) {
    this.email = email;
    return this;
  }
}
