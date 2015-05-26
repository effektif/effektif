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



/**
 * A collection of CLI commands, each of which has a Java implementation.
 */
public enum Command {
  COMPLETE(new CompleteCommand()),
  DEPLOY(new DeployCommand()),
  HELP(new HelpCommand()),
  QUIT(new QuitCommand()),
  START(new StartCommand()),
  TASK(new TaskCommand()),
  TASKS(new TasksCommand()),
  WORKFLOWS(new WorkflowsCommand());

  private CommandImpl implementation;

  private Command(CommandImpl implementation) {
    this.implementation = implementation;
  }

  public void execute(final CommandLine command, Configuration configuration, PrintWriter out) {
    implementation.execute(command, configuration, out);
  }
}
