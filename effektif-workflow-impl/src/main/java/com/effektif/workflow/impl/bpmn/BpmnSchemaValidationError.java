package com.effektif.workflow.impl.bpmn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API wrapper exception for BPMN schema validation errors.
 */
public class BpmnSchemaValidationError extends RuntimeException {

  private static final Logger log = LoggerFactory.getLogger(BpmnSchemaValidationError.class);
  public BpmnSchemaValidationError(Throwable cause) {
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
