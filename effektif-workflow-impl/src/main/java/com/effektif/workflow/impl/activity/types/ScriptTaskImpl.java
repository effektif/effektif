/*
 * Copyright 2014 Effektif GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.effektif.workflow.impl.activity.types;

import java.util.Map;

import com.effektif.workflow.api.activities.ScriptTask;
import com.effektif.workflow.api.workflow.Script;
import com.effektif.workflow.api.xml.XmlElement;
import com.effektif.workflow.impl.WorkflowParser;
import com.effektif.workflow.impl.activity.AbstractActivityType;
import com.effektif.workflow.impl.bpmn.BpmnReader;
import com.effektif.workflow.impl.bpmn.BpmnWriter;
import com.effektif.workflow.impl.data.TypedValueImpl;
import com.effektif.workflow.impl.script.ScriptImpl;
import com.effektif.workflow.impl.script.ScriptResult;
import com.effektif.workflow.impl.script.ScriptService;
import com.effektif.workflow.impl.workflow.ActivityImpl;
import com.effektif.workflow.impl.workflowinstance.ActivityInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class ScriptTaskImpl extends AbstractActivityType<ScriptTask> {

  private static final String BPMN_ELEMENT_NAME = "scriptTask";

  protected ScriptService scriptService;
  public Map<String, String> mappings;
  public ScriptImpl script;
  
  public ScriptTaskImpl() {
    super(ScriptTask.class);
  }

  @Override
  public ScriptTask readBpmn(XmlElement xml, BpmnReader reader) {
    if (!reader.isLocalPart(xml, BPMN_ELEMENT_NAME)) {
      return null;
    }
    Script script = new Script();
    script.setLanguage(reader.readStringValue(xml, "language"));
    script.setScript(reader.readStringValue(xml, "script"));
    script.setMappings(reader.readStringMappings(xml, "mapping", "scriptVariable", "workflowVariable"));
    return new ScriptTask().script(script);
  }

  @Override
  public void writeBpmn(ScriptTask task, XmlElement xml, BpmnWriter writer) {
    writer.setBpmnName(xml, BPMN_ELEMENT_NAME);
    if (task.getScript() != null) {
      writer.writeStringValue(xml, "language", task.getScript().getLanguage());
      writer.writeStringMappings(xml, "mapping", "scriptVariable", "workflowVariable", task.getScript().getMappings());
      writer.writeStringValueAsCData(xml, "script", task.getScript().getScript());
    }
  }

  @Override
  public void parse(ActivityImpl activityImpl, ScriptTask scriptTask, WorkflowParser parser) {
    super.parse(activityImpl, scriptTask, parser);
    this.scriptService = parser.getConfiguration(ScriptService.class);
    this.script = parser.parseScript(scriptTask.getScript());
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
