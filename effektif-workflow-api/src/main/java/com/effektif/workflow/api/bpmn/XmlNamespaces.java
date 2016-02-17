package com.effektif.workflow.api.bpmn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Value object that represents a set of XML namespace declarations.
 * There may be only one default namespace, whose key is null.
 *
 * @author Peter Hilton
 */
public class XmlNamespaces {

  /** Keyed on prefix rather than name, because prefixes are valid JSON object field names, for serialisation. */
  private final Map<String,URI> declarations = new HashMap<>();

  /** The default namespace, which does not have a prefix. */
  private URI defaultNamespace;

  /**
   * Declares the given namespace, whose name must be a valid URI, and whose prefix may only be empty if the default
   * namespace is not already declared.
   */
  public void add(String prefix, String name) throws URISyntaxException {
    if (prefix == null) {
      if (defaultNamespace != null) {
        throw new IllegalArgumentException(String.format("Namespace %s would overwrite default namespace", name));
      }
      defaultNamespace = new URI(name);
    }
    else {
      declarations.put(prefix, new URI(name));
    }
  }

  /**
   * Returns true if there is a namespace declared with the given name, which must be a well-formed URI.
   */
  public boolean hasNamespace(String name) {
    if (name == null) {
      return false;
    }
    return getPrefix(name) != null;
  }

  /**
   * Returns the names of all declared namespaces, including the default namespace.
   */
  public Collection<URI> getNames() {
    Set<URI> names = new HashSet<>(declarations.values());
    if (defaultNamespace != null) {
      names.add(defaultNamespace);
    }
    return names;
  }

  /**
   * Returns the namespace prefix for the given namespace name.
   */
  public String getPrefix(URI name) {
    if (name == null) {
      return null;
    }
    if (name.equals(defaultNamespace)) {
      return "";
    }
    return declarations.entrySet().stream()
      .filter(n -> n.getValue().equals(name))
      .findFirst()
      .map(n -> n.getKey())
      .orElse(null);
  }

  /**
   * Returns the namespace prefix for the given namespace name, which must be a well-formed URI.
   */
  public String getPrefix(String name) {
    try {
      return getPrefix(new URI(name));
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Returns true if the given namespace is the default namespace, which does not have a prefix.
   */
  public boolean isDefault(URI name) {
    return defaultNamespace != null && defaultNamespace.equals(name);
  }
}
