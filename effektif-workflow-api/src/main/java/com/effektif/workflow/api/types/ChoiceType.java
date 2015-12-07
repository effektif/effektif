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
package com.effektif.workflow.api.types;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.json.TypeName;

/**
 * Represents decision buttons at the end of a user task form - a choice that completes the task.
 * 
 * @author Tom Baeyens
 */
@TypeName("choice")
public class ChoiceType extends TextType {

  protected List<ChoiceOption> options;
  
  @Override
  public void readBpmn(BpmnReader r) {
    for (XmlElement optionElement : r.readElementsEffektif("option")) {
      r.startElement(optionElement);
      ChoiceOption option = new ChoiceOption();
      option.readBpmn(r);
      option(option);
      r.endElement();
    }
  }

  @Override
  public void writeBpmn(BpmnWriter w) {
    super.writeBpmn(w);
    if (options != null) {
      for (ChoiceOption option : options) {
        option.writeBpmn(w);
      }
    }
  }

  public List<ChoiceOption> getOptions() {
    return options;
  }
  public void setOptions(List<ChoiceOption> options) {
    this.options = options;
  }
  public ChoiceType option(String option) {
    option(new ChoiceOption().id(option));
    return this;
  }
  
  public ChoiceType option(ChoiceOption option) {
    if (options==null) {
      options = new ArrayList<>();
    }
    options.add(option);
    return this;
  }
}
