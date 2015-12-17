package com.effektif.workflow.impl.bpmn;

/**
 * API wrapper exception for BPMN schema validation errors.
 */
public class BpmnSchemaValidationError extends RuntimeException {

  public BpmnSchemaValidationError(Throwable cause) {
    super(cause.getMessage(), cause);
  }
}
