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
package com.effektif.workflow.api.bpmn;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.json.JsonIgnore;

/** 
 * XML DOM structure that Jackson can serialise to/from JSON for REST service and database persistence.
 *
 * @author Tom Baeyens
 */
public class XmlElement implements Comparable<XmlElement> {

  private static final Logger log = LoggerFactory.getLogger(XmlElement.class);

  /** prefix:localPart.  eg "e:subject" */
  public String name;

  /** Maps attribute names (prefix:localPart eg "e:type" or "type") to attribute values. */
  public Map<String, String> attributes;
  
  /** Maps namespace URIs to prefixes; a null value represents the default namespace. */
  public XmlNamespaces namespaces;
  
  public List<XmlElement> children;
  public String text;
  
  /** not persisted in the db */
  @JsonIgnore
  public XmlElement parent;
  /** not persisted in the db */
  @JsonIgnore
  public String namespaceUri;

  // name ///////////////////////////////////////////////////////////////////////////

  /**
   * Clears the BPMN element name, used to clean up unparsed BPMN after BPMN import when the name is redundant.
   */
  public void clearName() {
    this.name = null;
  }

  /**
   * Simplifies the BPMN XML by empty collections of attributes, child elements and extension elements.
   */
  public void cleanEmptyElements() {
    // Remove empty attributes list.
    if (attributes != null && attributes.isEmpty()) {
      attributes = null;
    }
    if (children != null) {
      // Recursively clean child elements.
      for (XmlElement childElement : children) {
        childElement.cleanEmptyElements();
      }

      // Remove empty child elements list.
      if (children.isEmpty()) {
        children = null;
      }
    }
  }

  public void setName(String namespaceUri, String localPart) {
    this.name = getNamespacePrefix(namespaceUri) + localPart;
    this.namespaceUri = namespaceUri;
  }

  public boolean is(String namespaceUri, String localPart) {
    return (this.namespaceUri!= null && this.namespaceUri.equals(namespaceUri) && name.equals(localPart))
           || (name.equals(getNamespacePrefix(namespaceUri)+localPart));
  }

  public String getName() {
    return name;
  }

  public String getLocalBPMNName() {
    String localPart = this.name.contains(":") ? this.name.substring(this.name.indexOf(':') + 1) : this.name;

    return this.is("http://www.omg.org/spec/BPMN/20100524/MODEL", localPart) ? localPart : null;
  }

  // namespaces ///////////////////////////////////////////////////////////////////////////
  
  /** maps namespace uris to prefixes */
  public XmlNamespaces getNamespaces() {
    return namespaces;
  }

  public void addNamespace(String namespaceUri, String prefix) {
    if (namespaces == null) {
      namespaces = new XmlNamespaces();
    }
    if ("".equals(prefix)) {
      prefix = null;
    }
    try {
      namespaces.add(prefix, namespaceUri);
    } catch (URISyntaxException e) {
      log.error(String.format("Cannot add XML namespace for invalid URI %s: %s", namespaceUri, e.getMessage()));
    }
  }
  
  public boolean hasNamespace(String namespaceUri) {
    if (this.namespaces!=null && this.namespaces.hasNamespace(namespaceUri)) {
      return true;
    }
    if (parent!=null) {
      return parent.hasNamespace(namespaceUri);
    }
    return false;
  }

  protected String getNamespacePrefix(String namespaceUri) {
    if (this.namespaces != null && this.namespaces.hasNamespace(namespaceUri)) {
      String prefix = this.namespaces.getPrefix(namespaceUri);
      return prefix == null || prefix.isEmpty() ? "" : prefix + ":";
    }
    if (parent!=null) {
      return parent.getNamespacePrefix(namespaceUri);
    }
    return "";
  }

  // elements ///////////////////////////////////////////////////////////////////////////

  public List<XmlElement> getChildren() {
    return children;
  }
  
  public List<XmlElement> removeElements(String namespaceUri, String localPart) {
    List<XmlElement> result = new ArrayList<>();
    if (this.children != null) {
      Iterator<XmlElement> iterator = this.children.iterator();
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
    if (children !=null) {
      Iterator<XmlElement> iterator = children.iterator();
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
   * Removes the specified element if it has no child elements (attributes are ignored).
   */
  public void removeEmptyElement(String namespaceUri, String localPart) {
    XmlElement element = getElement(namespaceUri, localPart);
    if (element != null) {
      List<XmlElement> childElements = element.getChildren();
      if (childElements == null || childElements.isEmpty()) {
        removeElement(namespaceUri, localPart);
      }
    }
  }

  /**
   * Returns the first element with the given name, or <code>null</code> if there isn’t one.
   */
  public XmlElement getElement(String namespaceUri, String localPart) {
    if (children ==null) {
      return null;
    }
    for (XmlElement childElement : children) {
      if (childElement.is(namespaceUri, localPart)) {
        return childElement;
      }
    }
    return null;
  }

  public XmlElement getOrCreateChildElement(String namespaceUri, String localPart) {
    return getOrCreateChildElement(namespaceUri, localPart, null);
  }

  /**
   * Returns either the first element with the given name, or the result of adding a new element if there wasn’t one.
   */
  public XmlElement getOrCreateChildElement(String namespaceUri, String localPart, Integer index) {
    XmlElement existingElement = getElement(namespaceUri, localPart);
    if (existingElement!=null) {
      return existingElement;
    }
    return createChildElement(namespaceUri, localPart);
  }
  
  public XmlElement createChildElement(String namespaceUri, String localPart) {
    XmlElement element = new XmlElement();
    element.parent = this;
    element.namespaceUri = namespaceUri;
    element.setName(namespaceUri, localPart);
    addChild(element);
    return element;
  }

  public void addChild(XmlElement xmlElement) {
    if (xmlElement !=null) {
      if (children ==null) {
        this.children = new ArrayList<>();
      }
      this.children.add(xmlElement);
      xmlElement.parent = this;
    }
  }

  // attributes ///////////////////////////////////////////////////////////////////////////

  public void addAttribute(String namespaceUri, String localPart, Object value) {
    if (attributes==null) {
      attributes = new LinkedHashMap<>();
    }
    String attributeName = null; 
    if (this.namespaceUri!=null && this.namespaceUri.equals(namespaceUri)) {
      attributeName = localPart;
    } else {
      attributeName = getNamespacePrefix(namespaceUri)+localPart;
    }
    attributes.put(attributeName, escapeAttributeValue(value));
  }

  public String removeAttribute(String namespaceUri, String localPart) {
    if (attributes==null) {
      return null;
    }
    if ( this.namespaceUri.equals(namespaceUri)
         && attributes.containsKey(localPart) ) {
      return unescapeXml(attributes.remove(localPart));
    }
    String attributeName = getNamespacePrefix(namespaceUri)+localPart;
    return unescapeXml(attributes.remove(attributeName));
  }

  public String getAttribute(String namespaceUri, String localPart) {
    if (attributes==null) {
      return null;
    }
    if ( this.namespaceUri.equals(namespaceUri)
         && attributes.containsKey(localPart) ) {
      return unescapeXml(attributes.get(localPart));
    }
    String attributeName = getNamespacePrefix(namespaceUri)+localPart;
    return unescapeXml(attributes.get(attributeName));
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }
  
  // text ///////////////////////////////////////////////////////////////////////////

  private static Pattern specialCharacters = Pattern.compile("[<>&]");

  /**
   * Adds text to the current element’s text node, wrapping the text in CDATA section if necessary, instead of escaping.
   */
  public void addCDataText(String value) {
    if (value != null) {
      boolean containsSpecialCharacters = specialCharacters.matcher(value).find();
      String wrappedText = containsSpecialCharacters ? "<![CDATA[" + value + "]]>" : value;
      this.text = this.text != null ? this.text + wrappedText : wrappedText;
    }
  }

  /**
   * Adds text to the current element’s text node, escaping XML special characters.
   */
  public void addText(Object value) {
    if (value != null) {
      String text = value.toString();
      if (!"".equals(text.trim())) {
        this.text = this.text != null ? this.text + escapeTextNode(text) : escapeTextNode(text);
      }
    }
  }

  public String getText() {
    return unescapeXml(text);
  }
  
  // other ///////////////////////////////////////////////////////////////////////////

  public boolean isEmpty() {
    if (attributes != null && !attributes.isEmpty()) {
      return false;
    }
    if (children != null && !children.isEmpty()) {
      return false;
    }
    if (text != null && !text.isEmpty()) {
      return false;
    }
    return true;
  }

  public boolean hasContent() {
    if (children !=null && !children.isEmpty()) {
      return false;
    }
    if (text!=null && !"".equals(text)) {
      return false;
    }
    return true;
  }

  private static String escapeTextNode(Object value) { return escapeXml(value, false); }
  private static String escapeAttributeValue(Object value) { return escapeXml(value, true); }

  private static String escapeXml(Object value, boolean replaceQuotes) {
    if (value == null) {
      return null;
    }
    String text = value.toString();
    String result = text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    if (replaceQuotes) {
      result = result.replaceAll("\"", "&#034;").replaceAll("'", "&#039;");
    }
    return result;
  }

  private static String unescapeXml(String value) {
    if (value == null) {
      return null;
    }
    return value.replaceAll("&#034;", "\"").replaceAll("&#039;", "'").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
      .replaceAll("&amp;", "&");
  }

  /**
   * Recursively fixes parent relationships, which are unset after deserialisation, such as when unparsed BPMN is read
   * from the database. If these parent relationships are not set, then the recursive namespace lookup fails.
   */
  public void setElementParents() {
    if (children == null) {
      return;
    }
    for (XmlElement child : children) {
      if (child.parent == null) {
        child.parent = this;
      }
      child.setElementParents();
    }
  }



  @Override
  public int compareTo(XmlElement other) {
    if (name == null || other == null || other.name == null) {
      return 0;
    }
    try {
      int elementOrdering = ElementOrder.valueOf(name.toUpperCase()).ordinal();
      int otherElementOrdering = ElementOrder.valueOf(other.name.toUpperCase()).ordinal();
      return elementOrdering - otherElementOrdering;
    }
    catch (IllegalArgumentException e) {
      // No element ordering defined for one of the elements.
      return 0;
    }
  }
}
