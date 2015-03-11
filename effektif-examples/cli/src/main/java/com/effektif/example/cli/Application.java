package com.effektif.example.cli;

import com.effektif.example.cli.command.Command;
import com.effektif.example.cli.command.CommandLine;
import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.impl.memory.MemoryConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
