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
package com.effektif.workflow.impl.bpmn.xml;

import java.io.Reader;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.effektif.workflow.api.xml.XmlElement;


/** uses a sax streaming parser to parse xml and 
 * generate {@link XmlElement our own jsonnable xml dom structure}. */
public class XmlReader {

  public Reader reader;
  public Stack<XmlElement> elementStack = new Stack<>();
  
  public static XmlElement parseXml(Reader reader) {
    XmlReader xmlParser = new XmlReader(reader);
    return xmlParser.readXml();
  }

  XmlReader(Reader reader) {
    this.reader = reader;
  }

  XmlElement readXml() {
    XmlElement root = null;
    try {
      XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
      XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(reader);
      while (xmlEventReader.hasNext()) {
        XMLEvent xmlEvent = xmlEventReader.nextEvent();
        if (xmlEvent.isStartDocument()){
          StartDocument startDocument = (StartDocument) xmlEvent;
          
        } else if (xmlEvent.isStartElement()){
          StartElement startElement = (StartElement)xmlEvent;
          XmlElement xmlElement = readXmlElement(startElement);
          if (elementStack.isEmpty()) {
            root = xmlElement;
          } else {
            elementStack.peek().addElement(xmlElement);
          }
          elementStack.push(xmlElement);
        } else if (xmlEvent.isEndElement()){
          elementStack.pop();
        } else if (xmlEvent.isCharacters()){
          elementStack.peek().addText(xmlEvent.asCharacters().getData()); 
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("XML parsing error: "+e.getMessage());
    }
    return root;
  }

  private XmlElement readXmlElement(StartElement startElement) {
    XmlElement xmlElement = new XmlElement();
    xmlElement.name = toString(startElement.getName());

    Iterator namespaces = startElement.getNamespaces();
    while (namespaces.hasNext()) {
      Namespace namespace = (Namespace) namespaces.next();
      xmlElement.addNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
    }
    Iterator attributes = startElement.getAttributes();
    while (attributes.hasNext()) {
      Attribute attribute = (Attribute) attributes.next();
      xmlElement.addAttribute(attribute.getName().toString(), attribute.getValue());
    }
    return xmlElement;
  }
  
  public static String toString(QName qname) {
    if (qname==null) {
      return null;
    }
    String prefix = qname.getPrefix();
    if (prefix!=null && !"".equals(qname.getPrefix())) {
      return prefix+":"+qname.getLocalPart();
    }
    return qname.getLocalPart();
  }
}
