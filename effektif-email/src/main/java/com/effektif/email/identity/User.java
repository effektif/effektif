/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.email.identity;

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
