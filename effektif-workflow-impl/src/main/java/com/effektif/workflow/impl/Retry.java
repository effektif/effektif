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


/** Implement retry with incremental backoff on operations that can fail. */
public abstract class Retry<T> {

  long wait = 50l;
  long attempts = 0;
  long maxAttempts = 4;
  long backoffFactor = 5;

  public Retry() {
  }

  public Retry(long wait, long attempts, long maxAttempts, long backoffFactor) {
    this.wait = wait;
    this.attempts = attempts;
    this.maxAttempts = maxAttempts;
    this.backoffFactor = backoffFactor;
  }

  /** invokes {@link #tryOnce()} till it produces a result or till the max attempts did not deliver a result.
   * Blocks the current thread in case retries have to be performed. */
  public T tryManyTimes() {
    T returnValue = tryOnce();
    while ( returnValue==null 
            && attempts <= maxAttempts ) {
      try {
        failedWaitingForRetry();
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        interrupted();
      }
      wait = wait * backoffFactor;
      attempts++;
      returnValue = tryOnce();
    }
    if (returnValue==null) {
      failedPermanent();
    }
    return returnValue;
  }

  /** returns null if the command failed, or not null if the command succeeded */
  public abstract T tryOnce();

  protected void failedWaitingForRetry() {
  }

  protected void interrupted() {
  }

  protected void failedPermanent() {
  }
}
