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
 * Constants for status codes used by {@link HttpMappedException} subclasses.
 */
public interface HttpStatusCode {
  int OK = 200;
  int CREATED = 201;
  int NO_CONTENT = 204;
  int BAD_REQUEST = 400;
  int UNAUTHORIZED = 401;
  int PAYMENT_REQUIRED = 402;
  int FORBIDDEN = 403;
  int NOT_FOUND = 404;
  int METHOD_NOT_ALLOWED = 405;
  int CONFLICT = 409;
  int PRECONDITION_FAILED = 412;
  int REQUEST_ENTITY_TOO_LARGE = 413;
  int INTERNAL_SERVER_ERROR = 500;
}
