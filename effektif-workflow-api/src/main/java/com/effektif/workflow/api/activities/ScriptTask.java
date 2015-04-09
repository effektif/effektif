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
package com.effektif.workflow.api.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.mapper.BpmnElement;
import com.effektif.workflow.api.mapper.BpmnReader;
import com.effektif.workflow.api.mapper.BpmnWriter;
import com.effektif.workflow.api.mapper.XmlElement;
import com.effektif.workflow.api.workflow.Script;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * A script task - JavaScript code that will be executed by the process engine.
 *
 * @see <a href="https://github.com/effektif/effektif/wiki/Script-Task">Script Task</a>
 * @author Tom Baeyens
 */
@JsonTypeName("scriptTask")
@BpmnElement("scriptTask")
public class ScriptTask extends NoneTask {

  protected Script script;
  
  @Override
  public void readBpmn(BpmnReader r) {
    r.startExtensionElements();
    script = new Script();
    script.setLanguage(r.readTextEffektif("language"));
    script.setScript(r.readTextEffektif("script"));
    List<XmlElement> mappingElements = r.readElementsEffektif("mapping");
    Map<String, String> mappings = null;
    for (XmlElement mappingElement: mappingElements) {
      r.startElement(mappingElement);
      if (mappings==null) {
        mappings = new HashMap<>();
      }
      String scriptVariableName = r.readStringAttributeEffektif("scriptVariableName");
      String workflowVariableId = r.readStringAttributeEffektif("workflowVariableId");
      mappings.put(scriptVariableName, workflowVariableId);
      r.endElement();
    }
    script.setMappings(mappings);
    r.endExtensionElements();
    super.readBpmn(r);
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    if (script!=null) {
      w.startExtensionElements();
      w.writeTextEffektif("language", script.getLanguage());
      w.writeTextEffektif("script", script.getScript());
      Map<String, String> mappings = script.getMappings();
      if (mappings!=null) {
        for (String scriptVariableName: mappings.keySet()) {
          String workflowVariableId = mappings.get(scriptVariableName);
          w.startElementEffektif("mapping");
          w.writeStringAttributeEffektif("scriptVariableName", scriptVariableName);
          w.writeStringAttributeEffektif("workflowVariableId", workflowVariableId);
          w.endElement();
        }
      }
      w.endExtensionElements();
    }
    super.writeBpmn(w);
  }

  @Override
  public ScriptTask id(String id) {
    super.id(id);
    return this;
  }

  public ScriptTask script(Script script) {
    this.script = script;
    return this;
  }
  
  public ScriptTask script(String script) {
    if (this.script == null) {
      this.script = new Script();
    }
    this.script.script(script);
    return this;
  }

  public ScriptTask scriptMapping(String scriptVariableName, String variableId) {
    if (this.script == null) {
      this.script = new Script();
    }
    this.script.mapping(scriptVariableName, variableId);
    return this;
  }
  
  public Script getScript() {
    return script;
  }
  
  public void setScript(Script script) {
    this.script = script;
  }
}
