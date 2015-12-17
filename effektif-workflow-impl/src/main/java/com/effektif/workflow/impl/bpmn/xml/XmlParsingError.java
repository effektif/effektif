package com.effektif.workflow.impl.bpmn.xml;

/**
 * API wrapper exception for XML parsing errors.
 */
public class XmlParsingError extends RuntimeException {

  public XmlParsingError(Throwable cause) {
    super(cause.getMessage(), cause);
  }
}
