package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;

import java.io.PrintWriter;

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
