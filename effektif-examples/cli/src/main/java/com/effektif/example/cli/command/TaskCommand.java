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
package com.effektif.example.cli.command;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.effektif.example.cli.TaskService;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.impl.json.JsonStreamMapper;

/**
 * Returns a list of open tasks for the running workflows.
 */
public class TaskCommand implements CommandImpl {

  /** Regular expression that matches a workflow instance ID and task ID, in capturing groups. */
  static final Pattern TASK_ID = Pattern.compile("([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})-([0-9])");

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final TaskId taskId = new TaskId(command.getArgument());
    final TaskService taskService = new TaskService(configuration);
    final ActivityInstance task = taskService.findById(taskId.getWorkflowInstanceId(), taskId.getActivityInstanceId());
    if (task == null) {
      out.println("No task found for ID:" + taskId);
    }
    else {
      final JsonStreamMapper jsonMapper = configuration.get(JsonStreamMapper.class);
      jsonMapper.pretty();
      out.println(jsonMapper.write(task));
    }
  }
}