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
package com.effektif.workflow.impl.exceptions;



/**
 * A {@link RuntimeException} that will be mapped to an HTTP error response in the REST API.
 * This means that the the messages and content must be safe to return to the client.
 *
 * TODO After effektif3 merge: rename subclasses’ checkTrue methods to the more fluent ‘unless’, add a ‘when’ with
 *
 * @author Tom Baeyens
 */
public abstract class HttpMappedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public HttpMappedException(String message) {
    super(message);
  }
  
  public HttpMappedException(String message, Throwable t) {
    super(message, t);
  }

  /**
   * The HTTP status code that will be used when sending HTTP responses as a result of this exception.
   */
  public abstract int getStatusCode();

  /**
   * Default JSON error response body for HTTP error responses.
   */
  public String getJsonBody() {
    return "{\"message\":\""+getMessage()+"\"}";
  }
}
