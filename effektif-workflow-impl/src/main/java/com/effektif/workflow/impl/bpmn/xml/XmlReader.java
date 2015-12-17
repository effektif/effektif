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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.effektif.workflow.api.bpmn.XmlElement;


/**
 * Uses a SAX streaming parser to parse XML and generate {@link XmlElement our own JSONnable XML DOM structure}.
 */
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
        
        /*if (xmlEvent.isStartDocument()){
          StartDocument startDocument = (StartDocument) xmlEvent;
        } else */
        
        XmlElement parent = !elementStack.isEmpty() ? elementStack.peek() : null;
        if (xmlEvent.isStartElement()){
          StartElement startElement = (StartElement)xmlEvent;
          
          // create the xml element
          XmlElement xmlElement = new XmlElement();
          if (parent==null) {
            root = xmlElement;
          } else {
            // establish parent-child relation
            parent.addElement(xmlElement);
            xmlElement.parent = parent;
          }

          // initialize namespaces
          Iterator namespaces = startElement.getNamespaces();
          while (namespaces.hasNext()) {
            Namespace namespace = (Namespace) namespaces.next();
            xmlElement.addNamespace(namespace.getNamespaceURI(), namespace.getPrefix());
          }

          // set the name (depends on the namespaces being initialized)
          QName qname = startElement.getName();
          xmlElement.setName(qname.getNamespaceURI(), qname.getLocalPart());
          
          // set the attributes (depends on the namespaces being initialized)
          Iterator attributes = startElement.getAttributes();
          while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            QName attributeQname = attribute.getName();
            xmlElement.addAttribute(attributeQname.getNamespaceURI(), attributeQname.getLocalPart(), attribute.getValue());
          }

          elementStack.push(xmlElement);
        } else if (xmlEvent.isEndElement()){
          elementStack.pop();
        } else if (xmlEvent.isCharacters()){
          parent.addText(xmlEvent.asCharacters().getData()); 
        }
      }
    } catch (XMLStreamException e) {
      throw new XmlParsingError(e);
    }
    return root;
  }
}
