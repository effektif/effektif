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
 * @author Tom Baeyens
 *
 * TODO Rename because ‘responsable’ is not a word, and about being used in an HTTP response, and nothing to do with
 * being ‘responsible’.
 */
public abstract class ResponsableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ResponsableException(String message) {
    super(message);
  }
  
  public ResponsableException(String message, Throwable t) {
    super(message, t);
  }
  
  public abstract int getStatusCode();

  public String getJsonBody() {
    return "{\"message\":\""+getMessage()+"\"}";
  }
}
