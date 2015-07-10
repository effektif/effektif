package com.effektif.workflow.test.api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.ExecutableWorkflow;
import com.effektif.workflow.api.workflow.Variable;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.test.WorkflowTest;

/**
 * @author Christian Wiggert
 */
public class VariableTest extends WorkflowTest {

  @Test
  public void testVariableDefaultValue() {
    Variable var = new Variable()
      .id("v")
      .type(new TextType())
      .defaultValue("This is a string!");

    ExecutableWorkflow workflow = new ExecutableWorkflow().variable(var);
    deploy(workflow);
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId()));

    Object value = workflowInstance.getVariableValue("v");
    assertEquals("The variable default value was ignored.", "This is a string!", value);
  }

  @Test
  public void testDefaultValueIsOverwritten() {
    Variable var = new Variable()
      .id("v")
      .type(new TextType())
      .defaultValue("This is a string!");

    ExecutableWorkflow workflow = new ExecutableWorkflow().variable(var);
    deploy(workflow);
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("v", "No, it's not!"));

    Object value = workflowInstance.getVariableValue("v");
    assertEquals("The trigger data didn't overwrite the default value.", "No, it's not!", value);
  }

}
