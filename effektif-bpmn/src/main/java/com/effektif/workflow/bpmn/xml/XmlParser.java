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

import java.io.Reader;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


public class XmlParser {

  public static final String BPMN_NAMESPACE_URI = "http://www.omg.org/spec/BPMN/20100524/MODEL";

  public Reader reader;
  public Stack<XmlElement> elementStack = new Stack<>();
  
  public static XmlElement parseXml(Reader reader) {
    XmlParser xmlParser = new XmlParser(reader);
    return xmlParser.parseXml();
  }

  XmlParser(Reader reader) {
    this.reader = reader;
  }

  XmlElement parseXml() {
    XmlElement rootElement = null;
    try {
      XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
      XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(reader);
      while (xmlEventReader.hasNext()) {
        XMLEvent xmlEvent = xmlEventReader.nextEvent();
        if (xmlEvent.isStartDocument()){
          StartDocument startDocument = (StartDocument) xmlEvent;
          System.err.println("start document "+startDocument);      
          
        } else if (xmlEvent.isStartElement()){
          StartElement startElement = (StartElement)xmlEvent;
          XmlElement xmlElement = new XmlElement(startElement);
          if (elementStack.isEmpty()) {
            rootElement = xmlElement;
          } else {
            elementStack.peek().addElement(xmlElement);
          }
          elementStack.push(xmlElement);
        } else if (xmlEvent.isEndElement()){
          elementStack.pop();
        } else if (xmlEvent.isCharacters()){
          // add the chars to elementStack.peek().text 
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("XML parsing error: "+e.getMessage());
    }
    return rootElement;
  }
}
