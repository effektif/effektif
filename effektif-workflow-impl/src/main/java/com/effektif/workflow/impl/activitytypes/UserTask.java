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
package com.effektif.workflow.impl.activitytypes;

import java.util.ArrayList;
import java.util.List;

import com.effektif.task.TaskService;
import com.effektif.workflow.api.activities.DefaultTask;
import com.effektif.workflow.api.activities.UserTask;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.impl.plugin.Binding;
import com.effektif.workflow.impl.plugin.ConfigurationField;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;
import com.effektif.workflow.impl.plugin.Label;
import com.effektif.workflow.impl.plugin.Validator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * @author Walter White
 */
@JsonTypeName("userTask")
public class UserTask extends DefaultTask {
  
  @JsonIgnore
  TaskService taskService;
  
  @ConfigurationField
  @Label("Name")
  Binding<String> name;
  
  @ConfigurationField
  @Label("Candidates")
  List<Binding<String>> candidates;
  
  @Override
  public void validate(Activity activity, Validator validator) {
    super.validate(activity, validator);
    this.taskService = validator.getServiceRegistry().getService(TaskService.class);
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    String taskName = activityInstance.getValue(name);
    if (taskName==null) {
      taskName = activityInstance.getActivity().getId().toString();
    }
    List<String> taskCandidateIds = activityInstance.getValue(candidates);
    String assigneeId = (taskCandidateIds!=null && taskCandidateIds.size()==1 ? taskCandidateIds.get(0) : null);
    
    taskService.newTask()
      .name(taskName)
      .assigneeId(assigneeId)
      .candidateIds(taskCandidateIds)
      .activityInstance(activityInstance)
      .save();
  }
  
  public UserTask name(String nameValue) {
    this.name = new Binding<String>().value(nameValue);
    return this;
  }

  public UserTask nameVariable(String nameVariableDefinitionId) {
    this.name = new Binding<String>().variableDefinitionId(nameVariableDefinitionId);
    return this;
  }

  public UserTask nameExpression(String nameExpressionText) {
    this.name = new Binding<String>().expression(nameExpressionText);
    return this;
  }

  public UserTask candidateId(String candidateId) {
    addCandidateBinding(new Binding<String>().value(candidateId));
    return this;
  }

  public UserTask candidateVariable(String candidateVariableId) {
    addCandidateBinding(new Binding<String>().variableDefinitionId(candidateVariableId));
    return this;
  }

  public UserTask candidateExpression(String candidateExpression) {
    addCandidateBinding(new Binding<String>().expression(candidateExpression));
    return this;
  }

  protected void addCandidateBinding(Binding<String> binding) {
    if (candidates==null) {
      candidates = new ArrayList<Binding<String>>();
    }
    candidates.add(binding);
  }
}
