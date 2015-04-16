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

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.task.Task;
import com.effektif.workflow.impl.mapper.deprecated.JsonMapper;

/**
 * Outputs details of the task with the given ID, in JSON format.
 */
public class TaskCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String taskId = command.getArgument();
    final Task task = configuration.getTaskService().findTaskById(new TaskId(taskId));
    final JsonMapper jsonMapper = configuration.get(JsonMapper.class);
    out.println(jsonMapper.writeToStringPretty(task));
    out.println();
  }
}
