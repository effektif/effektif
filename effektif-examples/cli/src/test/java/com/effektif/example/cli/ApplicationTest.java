package com.effektif.example.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the command line application by controlling the intput and output streams.
 */
public class ApplicationTest {

  private final PipedOutputStream inStream = new PipedOutputStream();
  private final PrintWriter inWriter = new PrintWriter(inStream, true);

  private final PipedInputStream outStream = new PipedInputStream();
  private final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

  private Thread application;

  public ApplicationTest() throws IOException {
    final BufferedReader in = new BufferedReader(new InputStreamReader(new PipedInputStream(inStream)));
    final PrintWriter out = new PrintWriter(new PipedOutputStream(outStream), true);
    final Application cli = new Application(in, out);
    application = new Thread(cli);
  }

  @Before
  public void start() {
    application.start();
  }

  @After
  public void stop() throws IOException, InterruptedException {
    if (application == null || !application.isAlive()) {
      return;
    }

    application.interrupt();
    // TODO Figure out why the application thread doesn’t finish running after the QUIT command.
//    throw new IllegalStateException("The application is still running.");
  }

  @Test(timeout = 1000)
  public void testWorkflows() throws IOException {
    readLines(Application.WELCOME);
    execute("workflows");
    readLines(
      "Deployed workflows:",
      "  release",
      "",
      "Running workflows:",
      "");
    execute("quit");
  }

  @Test(timeout = 1000)
  public void testTasks() throws IOException {
    readLines(Application.WELCOME);
    execute("tasks");
    readLines("Open tasks:", "");
    execute("quit");
  }

  @Test(timeout = 1000)
  public void test() throws IOException {
    readLines(Application.WELCOME);
    execute("start release");

    // List tasks (first task)
    execute("tasks");
    readLines(
      "Open tasks:",
      "  1: Move open issues (release)",
      "");

    // Complete first task
    execute("complete 1");

    // List tasks (first task)
    execute("tasks");
    readLines(
      "Open tasks:",
      "  2: Check continuous integration (release)",
      "");

    execute("quit");
  }

  /**
   * Executes a command by consuming the prompt and sending the command to the input.
   */
  private void execute(String command) throws IOException {
    read(Application.PROMPT);
    inWriter.println(command);
  }

  /**
   * Reads text from the command line output and checks that it is the expected string.
   */
  private void read(String expectedOutput) throws IOException {
    int length = expectedOutput.length();
    char[] buffer = new char[length];
    outReader.read(buffer, 0, length);
    assertEquals(expectedOutput, String.valueOf(buffer));
  }

  /**
   * Reads multiple lines from the output, to check multi-line command output.
   */
  private void readLines(String... expectedOutput) throws IOException {
    for (String line : expectedOutput) {
      read(line + System.lineSeparator());
    }
  }

}
