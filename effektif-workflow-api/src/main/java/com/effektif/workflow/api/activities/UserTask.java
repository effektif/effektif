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
package com.effektif.workflow.api.activities;

import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("userTask")
public class UserTask extends NoneTask {
  
  public static final String KEY_NAME = "name";
  public static final String KEY_CANDIDATE_IDS = "candidateIds";
  
  public UserTask name(String name) {
    inputValue(KEY_NAME, name);
    return this;
  }

  public UserTask nameVariableId(String nameVariableId) {
    inputVariableId(KEY_NAME, nameVariableId);
    return this;
  }

  public UserTask nameExpression(String nameExpression) {
    inputExpression(KEY_NAME, nameExpression);
    return this;
  }

  public UserTask candidateId(String candidateId) {
    inputValue(KEY_CANDIDATE_IDS, candidateId);
    return this;
  }

  public UserTask candidateIdVariableId(String candidateIdVariableId) {
    inputVariableId(KEY_CANDIDATE_IDS, candidateIdVariableId);
    return this;
  }

  public UserTask candidateIdExpression(String candidateIdExpression) {
    inputVariableId(KEY_CANDIDATE_IDS, candidateIdExpression);
    return this;
  }
}
