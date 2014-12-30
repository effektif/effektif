package com.effektif.workflow.impl;


import com.effektif.workflow.api.workflow.Transition;
import com.effektif.workflow.impl.plugin.ControllableActivityInstance;

public interface WorkflowInstanceEventListener {
  void started(ControllableActivityInstance instance);
  void ended(ControllableActivityInstance instance);
  void transition(ControllableActivityInstance instance, Transition transition);
}
