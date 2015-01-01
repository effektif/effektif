/* Copyright 2014 Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.impl.job;

import org.joda.time.LocalDateTime;


public interface JobBuilder {

  /** setting the key means the job service will ensure there is 
   * exactly 1 such job in the system when the job is saved. */
  JobBuilder key(String key);
  
  JobBuilder activityInstanceId(String activityInstanceId);
  
  JobBuilder duedate(LocalDateTime duedate);
  
  JobBuilder organizationId(String organizationId);
  
  JobBuilder processId(String processId);
  
  JobBuilder processDefinitionId(String processDefinitionId);
  
  JobBuilder processInstanceId(String processInstanceId);
  
  JobBuilder taskId(String taskId);

  void save();
}
