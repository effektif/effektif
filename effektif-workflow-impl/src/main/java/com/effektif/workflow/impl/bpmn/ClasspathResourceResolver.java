package com.effektif.workflow.impl.bpmn;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Loads files from the classpath, used instead of loading files from the filesystem directly when reading XML Schema
 * files that use <code>xsd:include</code> and <code>xsd:import</code> directives to load other schema files.
 */
public class ClasspathResourceResolver implements LSResourceResolver {

  private String basePath;

  /**
   * @param basePath Specifies the resource path within the classpath; does not start with slash; ends with a slash.
   */
  public ClasspathResourceResolver(String basePath) {
    this.basePath = basePath;
  }

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    LoadInput input = new LoadInput();
    String resourcePath = basePath + systemId;
    InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
    if (stream == null) {
      throw new IllegalArgumentException("Could not read resource as stream from path " + resourcePath);
    }
    input.setPublicId(publicId);
    input.setSystemId(systemId);
    input.setBaseURI(baseURI);
    input.setCharacterStream(new InputStreamReader(stream));
    return input;
  }
}
