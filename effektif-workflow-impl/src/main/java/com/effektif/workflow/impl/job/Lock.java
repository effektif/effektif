/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.effektif.workflow.impl.job;

import com.effektif.workflow.impl.Time;


/**
 * @author Tom Baeyens
 */
public class Lock {

  public Long time;
  public String owner;

  public Lock() {
  }

  public Lock(String owner) {
    this.time = Time.now();
    this.owner = owner;
  }
}
