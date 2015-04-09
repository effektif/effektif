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
import java.util.Iterator;
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

  /** prefix:localPart.  eg "e:subject" */
  public String name;

  /** maps attribute names (prefix:localPart eg "e:type" or "type") to attribute values */
  public Map<String, String> attributes;
  
  /** maps namespace uris to prefixes, null value represents the default namespace */
  public Map<String,String> namespaces;
  
  public List<XmlElement> elements;
  public String text;
  
  /** not persisted in the db */
  public XmlElement parent;
  /** not persisted in the db */
  public String namespaceUri;

  // name ///////////////////////////////////////////////////////////////////////////

  public void setName(String namespaceUri, String localPart) {
    this.name = getNamespacePrefix(namespaceUri)+localPart;
    this.namespaceUri = namespaceUri;
  }

  public boolean is(String namespaceUri, String localPart) {
    return (this.namespaceUri.equals(namespaceUri) && name.equals(localPart))
           || (name.equals(getNamespacePrefix(namespaceUri)+localPart));
  }

  public String getName() {
    return name;
  }

  // namespaces ///////////////////////////////////////////////////////////////////////////
  
  /** maps namespace uris to prefixes */
  public Map<String, String> getNamespaces() {
    return namespaces;
  }

  public void addNamespace(String namespaceUri, String prefix) {
    if (namespaces==null) {
      namespaces = new LinkedHashMap<>();
    }
    if ("".equals(prefix)) {
      prefix = null;
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

  // elements ///////////////////////////////////////////////////////////////////////////

  public List<XmlElement> getElements() {
    return elements;
  }
  
  public List<XmlElement> removeElements(String namespaceUri, String localPart) {
    List<XmlElement> result = new ArrayList<>();
    if (result!=null) {
      Iterator<XmlElement> iterator = this.elements.iterator();
      while (iterator.hasNext()) {
        XmlElement xmlElement = iterator.next();
        if (xmlElement.is(namespaceUri, localPart)) {
          iterator.remove();
          result.add(xmlElement);
        }
      }
    }
    return result;
  }
  
  public XmlElement removeElement(String namespaceUri, String localPart) {
    if (elements!=null) {
      Iterator<XmlElement> iterator = elements.iterator();
      while (iterator.hasNext()) {
        XmlElement xmlElement = iterator.next();
        if (xmlElement.is(namespaceUri, localPart)) {
          iterator.remove();
          return xmlElement;
        }
      }
    }
    return null;
  }
  
  /**
   * Returns the first element with the given name, or <code>null</code> if there isn’t one.
   */
  public XmlElement getElement(String namespaceUri, String localPart) {
    if (elements==null) {
      return null;
    }
    for (XmlElement childElement : elements) {
      if (childElement.is(namespaceUri, localPart)) {
        return childElement;
      }
    }
    return null;
  }

  /**
   * Returns either the first element with the given name, or the result of adding a new element if there wasn’t one.
   */
  public XmlElement getOrCreateChildElement(String namespaceUri, String localPart) {
    XmlElement existingElement = getElement(namespaceUri, localPart);
    if (existingElement!=null) {
      return existingElement;
    }
    return createElement(namespaceUri, localPart);
  }
  
  public XmlElement createElement(String namespaceUri, String localPart) {
    return createElement(namespaceUri, localPart, null);
  }

  public XmlElement createElementFirst(String namespaceUri, String localPart) {
    return createElement(namespaceUri, localPart, 0);
  }

  public XmlElement createElement(String namespaceUri, String localPart, Integer index) {
    XmlElement element = new XmlElement();
    element.namespaceUri = namespaceUri;
    addElement(element, index);
    element.setName(namespaceUri, localPart);
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


  // attributes ///////////////////////////////////////////////////////////////////////////

  public void addAttribute(String namespaceUri, String localPart, String value) {
    if (attributes==null) {
      attributes = new LinkedHashMap<>();
    }
    String attributeName = null; 
    if (this.namespaceUri!=null && this.namespaceUri.equals(namespaceUri)) {
      attributeName = localPart;
    } else {
      attributeName = getNamespacePrefix(namespaceUri)+localPart;
    }
    attributes.put(attributeName, value);
  }

  public String removeAttribute(String namespaceUri, String localPart) {
    if (attributes==null) {
      return null;
    }
    if ( this.namespaceUri.equals(namespaceUri)
         && attributes.containsKey(localPart) ) {
      return attributes.remove(localPart);
    }
    String attributeName = getNamespacePrefix(namespaceUri)+localPart;
    return attributes.remove(attributeName);
  }

  public String getAttribute(String namespaceUri, String localPart) {
    if (attributes==null) {
      return null;
    }
    if ( this.namespaceUri.equals(namespaceUri)
         && attributes.containsKey(localPart) ) {
      return attributes.get(localPart);
    }
    String attributeName = getNamespacePrefix(namespaceUri)+localPart;
    return attributes.get(attributeName);
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }
  
  // text ///////////////////////////////////////////////////////////////////////////

  public void addText(String text) {
    if (text!=null && !"".equals(text.trim())) {
      this.text = this.text!=null ? this.text+text : text;
    }
  }

  public String getText() {
    return text;
  }
  
  // other ///////////////////////////////////////////////////////////////////////////
  
  public boolean hasContent() {
    if (elements!=null && !elements.isEmpty()) {
      return false;
    }
    if (text!=null && !"".equals(text)) {
      return false;
    }
    return true;
  }
}
