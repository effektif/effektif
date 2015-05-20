/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */
package com.effektif.script;

import java.util.Map;

import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ScriptTaskImpl extends AbstractActivityType<ScriptTask> {

  protected ScriptService scriptService;
  public ScriptImpl script;
  
  public ScriptTaskImpl() {
    super(ScriptTask.class);
  }

  @Override
  public void parse(ActivityImpl activityImpl, ScriptTask scriptTask, WorkflowParser parser) {
    super.parse(activityImpl, scriptTask, parser);
    this.scriptService = parser.getConfiguration(ScriptService.class);
    
    Script script = scriptTask.getScript();
    if (script!=null) {
      try {
        this.script = scriptService.compile(script, parser);
      } catch (Exception e) {
        parser.addWarning("Invalid script '%s' : %s", script, e.getMessage());
      }
    }
  }
  

  @Override
  public void execute(ActivityInstanceImpl activityInstance) {
    if (script!=null) {
      ScriptResult scriptResult = script.run(activityInstance);
      Map<String, TypedValueImpl> updates = scriptResult!=null ? scriptResult.getUpdates() : null;
      if (scriptResult!=null && updates!=null) {
        for (String variableId: updates.keySet()) {
          TypedValueImpl typedValue = updates.get(variableId);
          activityInstance.setVariableValue(variableId, typedValue.value);
        }
      }
    }
    activityInstance.onwards();
  }
  
  @Override
  public boolean isAsync(ActivityInstanceImpl activityInstance) {
    return true;
  }
}
