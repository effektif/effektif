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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.bpmn.BpmnReadable;
import com.effektif.workflow.api.bpmn.BpmnReader;
import com.effektif.workflow.api.bpmn.BpmnWritable;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;


/** 
 * common data fields for any object in a {@link ExecutableWorkflow}. 
 * 
 * @author Tom Baeyens
 */
public abstract class Element extends Extensible implements BpmnReadable, BpmnWritable {
  
  protected static final Logger log = LoggerFactory.getLogger(Element.class);
  
  protected String name;
  protected String description;
  protected XmlElement bpmn;

  /**
   * Cleans up parsed BPMN to remove empty elements, and the BPMN element itself if empty, used after BPMN import.
   */
  public void cleanUnparsedBpmn() {
    if (bpmn != null) {
      bpmn.cleanEmptyElements();
      if (bpmn.isEmpty()) {
        bpmn = null;
      }
    }
  }

  @Override
  public void readBpmn(BpmnReader r) {
    name = r.readStringAttributeBpmn("name");
    description = r.readDocumentation();
    bpmn = r.getUnparsedXml();
  }
  
  @Override
  public void writeBpmn(BpmnWriter w) {
    w.writeStringAttributeBpmn("name", name);
    w.writeDocumentation(description);
  }
  
  /** human readable label used when visually displaying the element.
   * This corresponds to the BPMN name attribute. */
  public String getName() {
    return this.name;
  }
  /** @see #getName() */
  public void setName(String name) {
    this.name = name;
  }
  /** @see #getName() */
  public Element name(String name) {
    this.name = name;
    return this;
  }

  /** longer human readable description for the element.
   * This corresponds to the BPMN documentation/ attribute. */
  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public Element description(String description) {
    this.description = description;
    return this;
  }

  /** This way the BPMN parser can stick all non-parsed BPMN XML in the object. 
   * BPMN requires that all XML in an imported file that is not understood should 
   * be kept and included into the exported BPMN file. */
  public XmlElement getBpmn() {
    return this.bpmn;
  }
  /** @see #getBpmn() */
  public void setBpmn(XmlElement bpmn) {
    this.bpmn = bpmn;
  }
}
