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
 * Outputs a list of commands.
 */
public class HelpCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    out.println("Commands:");
    out.println("  complete [ID]   Completes the task with the given ID");
    out.println("  help            List commands");
    out.println("  start [ID]      Start the workflow with the given ID");
    out.println("  tasks           List open tasks (ID and name)");
    out.println("  task [ID]       Show details of the task with the given ID");
    out.println("  workflows       List deployed workflows (ID)");
    out.println("  quit            Exit command line");
    out.println("");
  }
}
