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
package com.effektif.workflow.impl.job;


/** job type with default retry strategy: 3 retries in total, 
 * one after 3 seconds one after an hour and one after 24 hours 
 * 
 * @author Tom Baeyens
 */
public abstract class AbstractJobType implements JobType {

  @Override
  public int getMaxRetries() {
    return 3;
  }

  @Override
  public int getRetryDelayInSeconds(long retry) {
    if (retry==1) {
      return 3; // 3 seconds
    } else if (retry==2) {
      return 60*60; // 1 hour
    } 
    return 24*60*60; // 24 hours
  }
}
