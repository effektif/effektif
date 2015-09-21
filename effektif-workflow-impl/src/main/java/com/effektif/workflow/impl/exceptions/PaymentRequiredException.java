package com.effektif.workflow.impl.exceptions;

/**
 * @author Christian Wiggert
 */
public class PaymentRequiredException extends HttpMappedException {

  public PaymentRequiredException(String message) {
    super(message);
  }

  @Override
  public int getStatusCode() {
    return HttpStatusCode.PAYMENT_REQUIRED;
  }

}
