package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;

import java.io.PrintWriter;

/**
 * A collection of CLI commands, each of which has a Java implementation.
 */
public enum Command {
  COMPLETE(new CompleteCommand()),
  DEPLOY(new DeployCommand()),
  HELP(new HelpCommand()),
  QUIT(new QuitCommand()),
  START(new StartCommand()),
  TASKS(new TasksCommand()),
  TASK(new TaskCommand()),
  WORKFLOWS(new WorkflowsCommand());

  private CommandImpl implementation;

  private Command(CommandImpl implementation) {
    this.implementation = implementation;
  }

  public void execute(final CommandLine command, Configuration configuration, PrintWriter out) {
    implementation.execute(command, configuration, out);
  }
}
