/* Copyright (c) 2014, Effektif GmbH.
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
 * limitations under the License. */
package com.effektif.workflow.impl.bpmn;

import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.bpmn.BpmnTypeChildElement;
import com.effektif.workflow.api.bpmn.XmlElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a mapping between a BPMN element and an Effektif API type, such as a service task.
 *
 * @author Tom Baeyens
 */
public class BpmnTypeMapping {

  String bpmnElementName;
  Map<String,String> bpmnTypeAttributes;
  String bpmnTypeChildElement;
  boolean bpmnTypeChildElementRequired;
  public Class<?> type;

  public String getBpmnElementName() {
    return bpmnElementName;
  }
  
  public void setBpmnElementName(String bpmnElementName) {
    this.bpmnElementName = bpmnElementName;
  }
  
  public Class< ? > getType() {
    return type;
  }

  public void setType(Class< ? > type) {
    this.type = type;
  }

  public Map<String, String> getBpmnTypeAttributes() {
    return bpmnTypeAttributes;
  }
  
  public void addBpmnTypeAttribute(String attribute, String value) {
    if (bpmnTypeAttributes==null) {
      bpmnTypeAttributes = new HashMap<String, String>();
    }
    bpmnTypeAttributes.put(attribute, value);
  }

  public void setBpmnTypeChildElement(String value) {
    bpmnTypeChildElement = value;
  }

  public void setBpmnTypeChildElementRequired(boolean bpmnTypeChildElementRequired) {
    this.bpmnTypeChildElementRequired = bpmnTypeChildElementRequired;
  }

  public Object instantiate() {
    try {
      return type.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns true if the given BPMN XML element is a match for this type mapping, assuming that it already matches by
   * BPMN element name (because mappings are stored per element name).
   */
  public boolean matches(XmlElement bpmn, BpmnReaderImpl reader) {
    boolean strict = true;
    return matchesBpmnTypeAttribute(bpmn, reader) && matchesBpmnTypeChildElement(bpmn, reader, strict);
  }

  /**
   * Returns true if the given BPMN XML element is a match for this type mapping, ignoring {@link BpmnTypeAttribute}
   * annotations (which are optional) and {@link BpmnTypeChildElement} annotations that are not required.
   */
  public boolean matchesNonStrict(XmlElement bpmn, BpmnReaderImpl reader) {
    boolean strict = false;
    return matchesBpmnTypeChildElement(bpmn, reader, strict);
  }

  /**
   * Returns true if the given BPMN XML element matches the a type mapping that specifies a {@link BpmnTypeAttribute}.
   */
  private boolean matchesBpmnTypeAttribute(XmlElement bpmnActivity, BpmnReaderImpl reader) {
    if (bpmnTypeAttributes == null) {
      return true;
    }
    for (String localPart: bpmnTypeAttributes.keySet()) {
      String typeValue = bpmnTypeAttributes.get(localPart);
      // get the attribute value in the xml element
      String xmlValue = bpmnActivity.getAttribute(Bpmn.EFFEKTIF_URI, localPart);
      if (typeValue.equals(xmlValue)) {
        // only if there is a match we read (==remove) the the attribute from the xml element
        bpmnActivity.removeAttribute(Bpmn.EFFEKTIF_URI, localPart);
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if the given BPMN XML element matches the a type mapping that specifies a {@link BpmnTypeChildElement}.
   */
  private boolean matchesBpmnTypeChildElement(XmlElement activityXml, BpmnReaderImpl reader, boolean strict) {
    if (bpmnTypeChildElement == null) {
      return true;
    }
    else if (!bpmnTypeChildElementRequired && !strict) {
      return true;
    }

    if (reader.readElementsBpmn(bpmnTypeChildElement).size() > 0) {
      return true;
    }

    return false;
  }
}
