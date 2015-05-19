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
package com.effektif.example.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.effektif.example.cli.command.Command;
import com.effektif.example.cli.command.CommandLine;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.memory.MemoryConfiguration;

/**
 * A workflow engine example with a command-line interface.
 *
 * <p>Based on <a href="https://github.com/codurance/task-list">Task List</a> by Codurance.</p>
 */
public class Application implements Runnable {

  private final BufferedReader in;
  private final PrintWriter out;
  private final Configuration configuration;

  protected static final String WELCOME = "Command line workflow example (enter ‘help’ to list commends).";
  protected final static String PROMPT = "> ";

  public Application(BufferedReader in, PrintWriter out) {
    this.in = in;
    this.out = out;
    configuration = new MemoryConfiguration();
    configuration.start();
  }

  /**
   * Starts the application interactively.
   */
  public static void main(String[] args) throws Exception {
    final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    final PrintWriter out = new PrintWriter(System.out);
    new Application(in, out).run();
  }

  /**
   * Runs the application, using the defined input and output.
   */
  @Override
  public void run() {
    // Deploy the workflow on start-up.
    Command.DEPLOY.execute(null, configuration, out);
    out.println(WELCOME);

    while (true) {
      out.print("> ");
      out.flush();
      String commandLineString = null;
      try {
        commandLineString = in.readLine().trim();
        if (!commandLineString.isEmpty()) {
          final CommandLine commandLine = CommandLine.parse(commandLineString);
          if (commandLine.isQuit()) {
            break;
          }
          commandLine.execute(configuration, out);
        }
      } catch (IllegalArgumentException e) {
        unknownCommand(commandLineString);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void unknownCommand(String commandLine) {
    out.println("Unknown command: " + commandLine);
    out.println();
  }
}
