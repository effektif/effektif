package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;

import java.io.PrintWriter;

/**
 * Outputs a quit message.
 */
public class QuitCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    out.println("Goodbye!");
  }
}
