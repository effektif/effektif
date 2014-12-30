/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.effektif.workflow.impl.job;

import org.joda.time.LocalDateTime;


/**
 * @author Tom Baeyens
 */
public class Lock {

  public LocalDateTime time;
  public String owner;

  public Lock() {
  }

  public Lock(String owner) {
    this.time = new LocalDateTime();
    this.owner = owner;
  }
}
