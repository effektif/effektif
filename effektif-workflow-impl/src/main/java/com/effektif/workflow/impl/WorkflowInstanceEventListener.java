package com.effektif.workflow.impl;


import com.effektif.workflow.impl.definition.TransitionImpl;
import com.effektif.workflow.impl.instance.ActivityInstanceImpl;

public interface WorkflowInstanceEventListener {
  void started(ActivityInstanceImpl instance);
  void ended(ActivityInstanceImpl instance);
  void transition(ActivityInstanceImpl instance, TransitionImpl transition);
}
