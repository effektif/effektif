package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;

import java.io.PrintWriter;

/**
 * A CLI command implementation for a given command line, which uses the given workflow engine and output.
 */
public interface CommandImpl {

  void execute(final CommandLine command, Configuration configuration, PrintWriter out);
}
