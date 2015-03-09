package com.effektif.example.cli.command;

import com.effektif.workflow.api.Configuration;
import com.effektif.workflow.api.WorkflowEngine;
import com.effektif.workflow.api.model.Message;
import com.effektif.workflow.api.query.WorkflowInstanceQuery;
import com.effektif.workflow.api.workflowinstance.ActivityInstance;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.WorkflowInstanceStore;
import com.effektif.workflow.impl.workflowinstance.WorkflowInstanceImpl;

import java.io.PrintWriter;
import java.util.List;

/**
 * Completes the task with the given ID.
 */
public class CompleteCommand implements CommandImpl {

  @Override
  public void execute(CommandLine command, Configuration configuration, PrintWriter out) {
    final String taskId = command.getArgument();
    final WorkflowEngine engine = configuration.getWorkflowEngine();
    final WorkflowInstanceStore instanceStore = configuration.get(WorkflowInstanceStore.class);
    final List<WorkflowInstanceImpl> instances = instanceStore.findWorkflowInstances(new WorkflowInstanceQuery());

    for (WorkflowInstanceImpl instance : instances) {
      WorkflowInstance workflowInstance = instance.toWorkflowInstance();
      for (ActivityInstance activity : workflowInstance.getActivityInstances()) {
        if (activity.getTaskId().equals(taskId)) {
          engine.send(new Message().
            workflowInstanceId(workflowInstance.getId()).
            activityInstanceId(activity.getId()));
        }
      }
    }
  }
}
