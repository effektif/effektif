/*
 * Copyright (c) 2015, Effektif GmbH.
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
 * @author Tom Baeyens
 *
 * TODO Move to engine {@link com.effektif.workflow.impl.exceptions}
 */
public class InternalServerException extends HttpMappedException {

  private static final long serialVersionUID = 1L;

  public InternalServerException(String message) {
    super(message);
  }
  
  public InternalServerException(String message, Throwable t) {
    super(message, t);
  }

  @Override
  public int getStatusCode() {
    return HttpStatusCode.INTERNAL_SERVER_ERROR;
  }

  public static void checkTrue(boolean condition, String message) {
    if (!condition) {
      throw new InternalServerException(message);
    }
  }

  public static void checkNotNull(Object object, String message) {
    checkTrue(object!=null, message);
  }
}
