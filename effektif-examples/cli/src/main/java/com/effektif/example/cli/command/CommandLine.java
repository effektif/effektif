package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;

import java.io.PrintWriter;

/**
 * A command line consisting of a command with a single argument.
 */
public class CommandLine {

  private final Command command;
  private final String argument;

  public CommandLine(Command command, String argument) {
    this.command = command;
    this.argument = argument;
  }

  /**
   * Executes this command line, using the given workflow engine configuration and output.
   */
  public void execute(Configuration configuration, PrintWriter out) {
    command.execute(this, configuration, out);
  }

  public String getArgument() {
    return argument;
  }

  public Command getCommand() {
    return command;
  }

  public boolean isQuit() {
    return Command.QUIT.equals(command);
  }

  /**
   * Parses a command line string into one or two parts.
   */
  public static CommandLine parse(String commandLine) {
    final String[] parts = commandLine.trim().split("\\s+", 2);
    final Command parsedCommand = Command.valueOf(parts[0].toUpperCase());
    if (parts.length > 1) {
      return new CommandLine(parsedCommand, parts[1]);
    }
    else {
      return new CommandLine(parsedCommand, "");
    }
  }
}
