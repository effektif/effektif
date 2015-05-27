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
package com.effektif.workflow.api.workflow;

import java.util.HashMap;
import java.util.Map;

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWritable;
import com.effektif.workflow.api.bpmn.BpmnWriter;

/**
 * @author Tom Baeyens
 */
public abstract class Trigger implements BpmnReadable, BpmnWritable {

  protected Map<String,OutputParameter> outputs;

  @Override
  public void readBpmn(BpmnReader r) {
    // TODO add output parameters
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    w.startElementEffektif("trigger");
    w.endElement();
    // TODO add output parameters
  }
  
  public Map<String, OutputParameter> getOutputs() {
    return outputs;
  }
  public void setOutputs(Map<String, OutputParameter> outputs) {
    this.outputs = outputs;
  }
  
  public Trigger output(String key, String outputVariableId) {
    output(key, new OutputParameter().variableId(outputVariableId));
    return this;
  }
  
  public Trigger output(String key, OutputParameter outputParameter) {
    if (outputs==null) {
      outputs = new HashMap<>();
    }
    outputs.put(key, outputParameter);
    return this;
  }
}
