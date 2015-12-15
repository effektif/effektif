package com.effektif.workflow.impl.bpmn.xml;

/**
 * API wrapper exception for XML parsing errors.
 */
public class InvalidXml extends RuntimeException {

  public InvalidXml(Throwable cause) {
    super(cause.getMessage(), cause);
  }
}
