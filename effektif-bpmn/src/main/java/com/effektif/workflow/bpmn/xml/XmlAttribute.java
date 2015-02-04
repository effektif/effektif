/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.bpmn.xml;

import java.io.Writer;
import javax.xml.stream.events.Attribute;

public class XmlAttribute {

  public String ns;
  public String name;
  public String value;
  
  public XmlAttribute() {
  }

  public XmlAttribute(Attribute attribute) {
    this.ns = attribute.getName().getPrefix();
    if ("".equals(this.ns)) {
      this.ns = null;
    }
    this.name = attribute.getName().getLocalPart();
    this.value = attribute.getValue();
    // log.debug("  attribute "+attribute+" ("+attribute.getClass().getName()+")");      
  }


  public XmlAttribute(String prefix, String name, String value) {
    this.ns = prefix;
    this.name = name;
    this.value = value;
  }

  public void writeTo(Writer writer) throws Exception {
    if (ns!=null) {
      writer.write(ns);
      writer.write(':');
    }
    writer.write(name);
    writer.write("=\"");
    writer.write(value);
    writer.write("\"");
  }
}
