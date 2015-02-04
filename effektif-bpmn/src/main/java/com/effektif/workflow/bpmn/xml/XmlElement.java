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

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;


public class XmlElement {

  public String ns;
  public String name;
  public List<XmlAttribute> attributes;
  public List<XmlNamespace> namespaces;
  public List<XmlElement> elements;
  public String text;

  public XmlElement() {
  }

  public XmlElement(String name) {
    this.name = name;
  }

  public void setAttribute(String prefix, String name, String value) {
    addAttribute(new XmlAttribute(prefix, name, value));
  }

  public XmlElement(StartElement startElement) {
    this.ns = startElement.getName().getPrefix();
    if ("".equals(this.ns)) {
      this.ns = null;
    }
    this.name = startElement.getName().getLocalPart();
    // log.debug("element "+(prefix!=null?prefix+":":"")+name);      
    Iterator namespaces = startElement.getNamespaces();
    while (namespaces.hasNext()) {
      addNamespace((Namespace) namespaces.next());
    }
    Iterator attributes = startElement.getAttributes();
    while (attributes.hasNext()) {
      addAttribute(new XmlAttribute((Attribute) attributes.next()));
    }
  }

  public void addNamespace(String prefix, String namespaceUri) {
    if (namespaces==null) {
      namespaces = new ArrayList();
    }
    namespaces.add(new XmlNamespace(prefix, namespaceUri));
  }

  void addNamespace(Namespace namespace) {
    addNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
  }

  public void addAttribute(XmlAttribute xmlAttribute) {
    if (attributes==null) {
      attributes = new ArrayList<>();
    }
    attributes.add(xmlAttribute);
  }

  public void addElement(XmlElement xmlElement) {
    if (elements==null) {
      this.elements = new ArrayList<>();
    }
    this.elements.add(xmlElement);
  }

  public String getAttribute(String name) {
    return getAttribute(name, false);
  }
  
  public String removeAttribute(String name) {
    return getAttribute(name, true);
  }
  
  public String getAttribute(String name, boolean remove) {
    if (attributes!=null) {
      for (XmlAttribute attribute: attributes) {
        if (name.equals(attribute.name)) {
          if (remove) {
            attributes.remove(attribute);
          }
          return attribute.value;
        }
      }
    }
    return null;
  }

  public boolean is(String name) {
    return name.equals(this.name);
  }
  
  public void writeTo(Writer writer) {
    writeTo(writer, 0);
  }
  public void writeTo(Writer writer, int indentation) {
    try {
      writeIndentation(writer, indentation);
      writer.write('<');
      if (ns!=null) {
        writer.write(ns);
        writer.write(':');
      }
      writer.write(name);
      writer.write(' ');
      if (attributes!=null) {
        for (XmlAttribute attribute: attributes) {
          attribute.writeTo(writer);
          writer.write(' ');
        }
      }
      if (namespaces!=null) {
        for (XmlNamespace namespace: namespaces) {
          writer.write("xmlns");
          if (namespace.prefix!=null) {
            writer.write(':');
            writer.write(namespace.prefix);
          }
          writer.write("=\"");
          writer.write(namespace.uri);
          writer.write("\"");
          writer.write(' ');
        }
      }
      writer.write('>');
      if (elements!=null) {
        for (XmlElement element: elements) {
          writer.write("\n");
          element.writeTo(writer, indentation+1);
        }
      }
      writer.write("\n");
      writeIndentation(writer, indentation);
      writer.write("</");
      if (ns!=null) {
        writer.write(ns);
        writer.write(':');
      }
      writer.write(name);
      writer.write(">");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void writeIndentation(Writer writer, int indentation) throws Exception {
    for (int i=0; i<indentation; i++) {
      writer.write("  ");
    }
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    writeTo(writer);
    writer.flush();
    return writer.toString();
  }

  public XmlNamespace findNamespaceByUri(String namespaceUri) {
    if (namespaces!=null) {
      for (XmlNamespace namespace: namespaces) {
        if (namespaceUri.equals(namespace.uri)) {
          return namespace;
        }
      }
    }
    return null;
  }
}
