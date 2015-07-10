package com.effektif.workflow.test.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.activities.StartEvent;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;

/**
 * Tests combinations of parallel gateways and embedded subprocesses.
 *
 * @author Peter Hilton
 */
public class ParallelEmbeddedSubprocessTest extends WorkflowTest {

  /**
   * Tests parallel tasks inside a subprocess.
   * <pre>
   *
   *     ┌──────────────────────┐
   *     │ subprocess           │
   *     │                      │
   *  ◯──┤ ◯─→<+>─→[s1]─→<+>─→◯ ├─→◯
   *     │     │          ↑     │
   *     │     └──→[s2]───┘     │
   *     └──────────────────────┘
   *
   * </pre>
   */
  @Test
  public void testSubprocessParallelGateway() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("subprocess"))
      .activity("subprocess", new EmbeddedSubprocess()
        .activity("subprocessstart", new StartEvent()
          .transitionTo("fork"))
        .activity("fork", new ParallelGateway()
          .transitionTo("s1")
          .transitionTo("s2"))
        .activity("s1", new ReceiveTask()
          .transitionTo("join"))
        .activity("s2", new ReceiveTask()
          .transitionTo("join"))
        .activity("join", new ParallelGateway()
          .transitionTo("endSubprocess"))
        .activity("endSubprocess", new EndEvent())
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "subprocess", "s1", "s2");

    workflowInstance = endTask(workflowInstance, "s1");
    workflowInstance = endTask(workflowInstance, "s2");
    assertTrue(workflowInstance.isEnded());
  }

  /**
   * Tests a subprocess inside parallel tasks.
   * <pre>
   *
   *          ┌────────────┐
   *          │ subprocess │
   *  ◯─→<+>─→┤            ├─→<+>─→◯
   *      │   │ [s1]─→[s2] │   ↑
   *      │   └────────────┘   │
   *      │                    │
   *      └───────→[t1]────────┘
   *
   * </pre>
   */
  @Test
  public void testParallelGatewaySubprocess() {
    // @formatter:off
    ExecutableWorkflow workflow = new ExecutableWorkflow()
      .activity("start", new StartEvent()
        .transitionTo("fork"))
      .activity("fork", new ParallelGateway()
        .transitionTo("subprocess")
        .transitionTo("t1"))
      .activity("subprocess", new EmbeddedSubprocess()
        .activity("s1", new ReceiveTask()
          .transitionTo("s2"))
        .activity("s2", new ReceiveTask())
        .transitionTo("join"))
      .activity("t1", new ReceiveTask()
        .transitionTo("join"))
      .activity("join", new ParallelGateway()
        .transitionTo("end"))
      .activity("end", new EndEvent());
    // @formatter:on

    deploy(workflow);

    WorkflowInstance workflowInstance = start(workflow);
    assertOpen(workflowInstance, "subprocess", "s1", "t1");

    workflowInstance = endTask(workflowInstance, "s1");
    workflowInstance = endTask(workflowInstance, "s2");
    assertOpen(workflowInstance, "t1");

    workflowInstance = endTask(workflowInstance, "t1");
    assertTrue(workflowInstance.isEnded());
  }
}
