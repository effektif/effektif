package com.effektif.workflow.impl.bpmn.xml;

/**
 * API wrapper exception for XML parsing errors.
 */
public class XmlParsingError extends RuntimeException {

  public XmlParsingError(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  /**
   * Returns the root cause message, by unwinding nested exceptions.
   */
  @Override
  public String getMessage() {
    Throwable rootCause = getCause();
    while (rootCause.getCause() != null) {
      rootCause = rootCause.getCause();
    }
    return rootCause.getMessage();
  }
}
