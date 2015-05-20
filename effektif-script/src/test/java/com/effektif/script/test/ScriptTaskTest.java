/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.script.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.effektif.script.ScriptTask;
import com.effektif.workflow.api.model.TriggerInstance;
import com.effektif.workflow.api.types.TextType;
import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.data.types.ObjectType;
import com.effektif.workflow.test.WorkflowTest;


/**
 * @author Tom Baeyens
 */
public class ScriptTaskTest extends WorkflowTest {
  
  @Test
  public void testScript() {
    Workflow workflow = new Workflow()
      .variable("n", new TextType())
      .variable("m", new TextType())
      .activity("s", new ScriptTask()
        .script("message = 'Hello ' + name;")
        .scriptMapping("name", "n")
        .scriptMapping("message", "m"));

    deploy(workflow);
    
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("n", "World"));

    assertEquals("Hello World", workflowInstance.getVariableValue("m"));
  }

  @Test
  public void testScriptDereferencing() {
    Map<String,Object> johndoe = new HashMap<>();
    johndoe.put("fullName", "John Doe");

    Workflow workflow = new Workflow()
      .variable("user", new ObjectType())
      .variable("name", new TextType())
      .activity("s", new ScriptTask()
        .script("name = user.fullName;"));

    deploy(workflow);
    
    WorkflowInstance workflowInstance = workflowEngine.start(new TriggerInstance()
      .workflowId(workflow.getId())
      .data("user", johndoe));

    assertEquals("John Doe", workflowInstance.getVariableValue("name"));
  }
}
