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



/** Use {@link AbstractJobType} to leverage the default 
 * retry strategy.
 * 
 * @author Walter White
 */
public interface JobType {

  /** one less then the total number of attempts before giving up.
   * So returning 0 means only 1 attempt is done and no retries.  */
  int getMaxRetries();
  
  /** @param retry indicates the sequence number for the retry to be 
   * scheduled for the job to implement incremental backoff, so it 
   * will be 1 the first time. */
  int getRetryDelayInSeconds(long retry);

  /** invoked when a job is due.
   * RuntimeException's will cause the JobService to retry as 
   * configured with {@link #getMaxRetries()} and {@link #getRetryDelayInSeconds(long)}.
   * So execute should NOT call {@link JobController#rescheduleFor(org.joda.time.LocalDateTime) reschedule}
   * methods for retry. */ 
  void execute(JobController jobController);
}
