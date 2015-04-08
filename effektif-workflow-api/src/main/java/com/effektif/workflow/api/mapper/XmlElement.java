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
package com.effektif.workflow.api.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 
 * XML DOM structure that is jsonnable with Jackson
 * so that it can be serialized to/from JSON for REST service and database persistence.
 *
 * @author Tom Baeyens
 */
public class XmlElement {

  public String name;
  public Map<String, String> attributes;
  /** maps namespace uris to prefixes, null value represents the default namespace */
  public Map<String,String> namespaces;
  public List<XmlElement> elements;
  public String text;
  
  public XmlElement parent;
  public String namespaceUri;

  public XmlElement() {
  }
  
  public XmlElement(String namespaceUri, String localPart) {
    setName(namespaceUri, localPart);
  }
  
  public void setName(String namespaceUri, String localPart) {
    this.name = getNamespacePrefix(namespaceUri)+localPart;
    this.namespaceUri = namespaceUri;
  }

  public void addNamespace(String namespaceUri, String prefix) {
    if (namespaces==null) {
      namespaces = new LinkedHashMap<>();
    }
    namespaces.put(namespaceUri, prefix);
  }
  
  public boolean hasNamespace(String namespaceUri) {
    if (this.namespaces!=null && this.namespaces.containsKey(namespaceUri)) {
      return true;
    }
    if (parent!=null) {
      return parent.hasNamespace(namespaceUri);
    }
    return false;
  }

  protected String getNamespacePrefix(String namespaceUri) {
    if (this.namespaces!=null && this.namespaces.containsKey(namespaceUri)) {
      String prefix = this.namespaces.get(namespaceUri);
      if (prefix==null) {
        return "";
      } else {
        return prefix+":";
      }
    }
    if (parent!=null) {
      return parent.getNamespacePrefix(namespaceUri);
    }
    return "";
  }

  public void addAttribute(String namespaceUri, String localPart, String value) {
    if (attributes==null) {
      attributes = new LinkedHashMap<>();
    }
    String attributeName = null; 
    if (this.namespaceUri==null || this.namespaceUri.equals(namespaceUri)) {
      attributeName = getNamespacePrefix(namespaceUri)+localPart;
    } else {
      attributeName = localPart;
    }
    attributes.put(attributeName, value);
  }

  public XmlElement createElement(String namespaceUri, String localPart) {
    return createElement(namespaceUri, localPart, null);
  }

  public XmlElement createElementFirst(String namespaceUri, String localPart) {
    return createElement(namespaceUri, localPart, 0);
  }

  public XmlElement createElement(String namespaceUri, String localPart, Integer index) {
    XmlElement element = new XmlElement(namespaceUri, localPart);
    element.namespaceUri = namespaceUri;
    addElement(element, index);
    return element;
  }

  public void addElement(XmlElement xmlElement) {
    addElement(xmlElement, null);
  }

  public void addElementFirst(XmlElement xmlElement) {
    addElement(xmlElement, 0);
  }

  public void addElement(XmlElement xmlElement, Integer index) {
    if (xmlElement!=null) {
      if (elements==null) {
        this.elements = new ArrayList<>();
      }
      if (index!=null) {
        this.elements.add(index, xmlElement);
      } else {
        this.elements.add(xmlElement);
      }
      xmlElement.parent = this;
    }
  }

  /**
   * Returns the first element with the given name, or <code>null</code> if there isn’t one.
   */
  public XmlElement findChildElement(String elementName) {
    if (elements==null) {
      return null;
    }
    for (XmlElement childElement : elements) {
      if (childElement.is(elementName)) {
        return childElement;
      }
    }
    return null;
  }
  /**
   * Returns either the first element with the given name, or the result of adding a new element if there wasn’t one.
   */
  public XmlElement findOrAddChildElement(String elementName) {
    XmlElement childElement = findChildElement(elementName);
    if (childElement == null) {
      childElement = new XmlElement(elementName);
      addElement(childElement);
    }
    return childElement;
  }

  public boolean is(String name) {
    return name.equals(this.name);
  }
  
  public void addText(String text) {
    if (text!=null && !"".equals(text.trim())) {
      this.text = this.text!=null ? this.text+text : text;
    }
  }

  public boolean hasContent() {
    if (elements!=null && !elements.isEmpty()) {
      return false;
    }
    if (text!=null && !"".equals(text)) {
      return false;
    }
    return true;
  }

  public String removeAttribute(String name) {
    return attributes!=null ? attributes.remove(name) : null;
  }

  public String getAttribute(String name) {
    return attributes!=null ? attributes.get(name) : null;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }
  
  /** maps namespace uris to prefixes */
  public Map<String, String> getNamespaces() {
    return namespaces;
  }

  public List<XmlElement> getElements() {
    return elements;
  }

  public String getText() {
    return text;
  }
}
